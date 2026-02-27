package classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SRTF implements Escalonador {
    PriorityQueue<InstanciaTarefa> filaPronto = new PriorityQueue<>(Comparator.comparingInt(InstanciaTarefa::getCtRestante));
    List<InstanciaTarefa> processador;
    List<Tarefa> tarefasOriginais;
    List<InstanciaTarefa> concluidas = new ArrayList<>();

    public SRTF(List<Tarefa> tarefasOriginais) {
        this.tarefasOriginais = tarefasOriginais;
        this.processador = new ArrayList<>();
    }
    
    public double calcularUtilizacao(List<Tarefa> tarefas) {
        double utilizacao = 0;
        for (Tarefa t : tarefas) {
            utilizacao += (double) t.getCt() / t.getPeriodo();
        }
        return utilizacao;
    }

    @Override
    public void simular(List<Tarefa> listaTarefas, int tempoSim) {
        int instante = 0;
        
        double utilizacao = calcularUtilizacao(tarefasOriginais);
        System.out.printf("Utilização do sistema: %.2f%n\n", utilizacao);

        do {
        	//Liberação periódica das tarefas
            for (Tarefa t : tarefasOriginais) {
                if ((instante - t.getOffset()) % t.getPeriodo() == 0 && instante >= t.getOffset()) {
                    InstanciaTarefa nova = new InstanciaTarefa(t, instante);
                    filaPronto.add(nova);
                    System.out.println("Instante " + instante + ": Tarefa ID " + nova.getIdInstancia() + " liberada.");
                }
            }

            //Verifica preempção
            if (!processador.isEmpty()) {
                InstanciaTarefa atual = processador.get(0);
                InstanciaTarefa menorFila = filaPronto.peek();

                if (menorFila != null && menorFila.getCtRestante() < atual.getCtRestante()) {
                    processador.remove(0);
                    filaPronto.add(atual);

                    InstanciaTarefa novo = filaPronto.poll();
                    processador.add(novo);

                    System.out.println("Instante " + instante + ": Preempção. Tarefa ID " + atual.getIdInstancia() +
                            " saiu do processador. Tarefa ID " + novo.getIdInstancia() + " entrou no processador.");
                }
            }

            //Se processador vazio, pega o menor da fila
            if (processador.isEmpty() && !filaPronto.isEmpty()) {
                InstanciaTarefa menor = filaPronto.poll();
                processador.add(menor);
                System.out.println("Instante " + instante + ": Tarefa ID " + menor.getIdInstancia() + " entrou no processador.");
            }
            
            //Verifica inversão por CT restante
            if (!processador.isEmpty() && !filaPronto.isEmpty()) {
                InstanciaTarefa atual = processador.get(0);
                InstanciaTarefa menorFila = filaPronto.stream()
                    .min(Comparator.comparingInt(InstanciaTarefa::getCtRestante))
                    .orElse(null);

                if (menorFila != null && menorFila.getCtRestante() < atual.getCtRestante()) {
                    System.out.printf("Inversão de prioridade SRTF no instante %d: ID %d (CT=%d) ultrapassou ID %d (CT=%d)%n",
                        instante,
                        atual.getIdInstancia(), atual.getCtRestante(),
                        menorFila.getIdInstancia(), menorFila.getCtRestante());
                }
            }

            //Processando a tarefa
            if (!processador.isEmpty()) {
                InstanciaTarefa atual = processador.get(0);
                atual.processa();

                System.out.println("Instante " + instante + ": Processando tarefa ID " + atual.getIdInstancia() +
                        ", CT restante = " + (atual.getCtRestante() + 1) + " -> " + atual.getCtRestante());

                if (atual.concluida()) {
                	atual.setConclusao(instante + 1);
                    concluidas.add(atual);
                    processador.remove(0);
                    System.out.println("Instante " + instante + ": Tarefa ID " + atual.getIdInstancia() + " concluída.");
                }
            }
            mostraFilaPronto();
            instante++;
        } while (instante <= tempoSim);              
    }

    @Override
    public void mostraFilaPronto() {
        List<InstanciaTarefa> lista = new ArrayList<>(filaPronto);
        lista.sort(Comparator.comparingInt(InstanciaTarefa::getCtRestante));

        if (lista.isEmpty()) {
            System.out.println("Fila de Pronto: [vazia]\n");
        } else {
            System.out.print("Fila de Pronto: [");
            for (InstanciaTarefa t : lista) {
                System.out.print("(TarefaID=" + t.getIdInstancia() +
                                 ", CT=" + t.getCtRestante() +
                                 ", P=" + t.getTarefa().getPeriodo() + ") ");
            }
            System.out.println("]\n");
        }
    }
    
    @Override
    public void calcularTAT() {
        //Mapeia cada tarefa original ao total de turnaround e quantidade
        class Estatistica {
            int totalTurnaround = 0;
            int contadorConcluidas = 0;
        }

        java.util.Map<Integer, Estatistica> mapa = new java.util.HashMap<>();
        
        //Percorre todas as concluidas e soma os TATs
        for (InstanciaTarefa instancia : concluidas) {
            int idOriginal = instancia.getTarefa().getId();
            int turnaround = instancia.getConclusao() - instancia.getInstanteChegada();

            mapa.putIfAbsent(idOriginal, new Estatistica());
            Estatistica est = mapa.get(idOriginal);
            est.totalTurnaround += turnaround;
            est.contadorConcluidas++;
        }

        //Exibe o TAT medio de cada tarefa original
        System.out.println("\n=== Turnaround médio por tarefa ===");
        for (var entry : mapa.entrySet()) {
            int id = entry.getKey();
            Estatistica est = entry.getValue();
            double media = (double) est.totalTurnaround / est.contadorConcluidas;
            System.out.printf("Tarefa ID %d: Média = %.2f (%d instâncias)%n", id, media, est.contadorConcluidas);
        }
        
        //Percorre as concluidas e soma os TATs de todas as instancias
        int somaTAT = 0;
        int totalInstancias = concluidas.size();
        for (InstanciaTarefa instancia : concluidas) {
            somaTAT += instancia.getTurnaroundTime();
        }

        //Exibe o TAT do sistema
        if (totalInstancias > 0) {
            double tatMedioSistema = (double) somaTAT / totalInstancias;
            System.out.printf("\nTurnaround médio do sistema: %.2f (%d instâncias concluídas)%n",
                              tatMedioSistema, totalInstancias);
        } else {
            System.out.println("\nNenhuma tarefa foi concluída. TAT médio do sistema não pode ser calculado.");
        }
    }
    
    @Override
    public void calcularWT() {
        int[] totalWT = new int[200];
        int[] contagem = new int[200];
        int somaWT = 0;
        int totalInstancias = concluidas.size();

        //Variveis de maior e menor WT
        double menorWT = Double.MAX_VALUE;
        double maiorWT = Double.MIN_VALUE;
        int idMenor = -1, idMaior = -1;

        //Calculo do WT de cada instancia concluida
        for (InstanciaTarefa instancia : concluidas) {
            int id = instancia.getTarefaOriginalId();
            int ct = instancia.getComputationTime();
            int wt = (instancia.getConclusao() - instancia.getInstanteChegada()) - ct;

            totalWT[id] += wt;
            contagem[id]++;
            somaWT += wt;
        }

        //Exibe o WT medio de cada tarefa original
        System.out.println("\n=== Waiting Time médio por tarefa ===");
        for (int id = 0; id < totalWT.length; id++) {
            if (contagem[id] > 0) {
                double media = (double) totalWT[id] / contagem[id];
                System.out.printf("Tarefa ID %d: Média = %.2f (%d instâncias)%n", id, media, contagem[id]);

                if (media > maiorWT) {
                    maiorWT = media;
                    idMaior = id;
                }

                if (media < menorWT) {
                    menorWT = media;
                    idMenor = id;
                }
            }
        }
        
        //Calcula o WT medio do sistema e exibe
        if (totalInstancias > 0) {
            double mediaSistema = (double) somaWT / totalInstancias;
            System.out.printf("\nWaiting Time médio do sistema: %.2f (%d instâncias concluídas)%n",
                              mediaSistema, totalInstancias);

            System.out.printf("Tarefa com MAIOR WT médio: ID %d → %.2f%n", idMaior, maiorWT);
            System.out.printf("Tarefa com MENOR WT médio: ID %d → %.2f%n", idMenor, menorWT);
        } else {
            System.out.println("\nNenhuma tarefa foi concluída. WT médio do sistema não pode ser calculado.");
        }
    }
    
    @Override
    public void verificarStarvation() {
        boolean starvationDetectada = false;

        //Verifica se cada tarefa original teve uma instancia concluida 
        System.out.println("\n=== Verificação de Starvation ===");
        for (Tarefa tarefa : tarefasOriginais) {
            int id = tarefa.getId();
            int count = 0;

            //Incrementa o contador de quantas foram concluidas de determinada tarefa original
            for (InstanciaTarefa instancia : concluidas) {
                if (instancia.getTarefaOriginalId() == id) {
                    count++;
                }
            }

            //Se o contador ficou zerado, houve starvation de alguma tarefa
            if (count == 0) {
                starvationDetectada = true;
                System.out.printf("Tarefa ID %d sofreu starvation (nenhuma instância concluída)%n", id);
            }
        }
        if (!starvationDetectada) {
            System.out.println("Nenhuma tarefa sofreu starvation.");
        }
    }

}