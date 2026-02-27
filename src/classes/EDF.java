package classes;

import java.util.*;

public class EDF implements Escalonador {
    PriorityQueue<InstanciaTarefa> filaPronto;
    List<InstanciaTarefa> processador = new LinkedList<>();
    List<Tarefa> tarefasOriginais;
    List<InstanciaTarefa> concluidas = new ArrayList<>();

    Map<Integer, Integer> ativacoes = new HashMap<>();
    Map<Integer, Integer> perdasDeadline = new HashMap<>();

    public EDF(List<Tarefa> tarefasOriginais) {
        this.tarefasOriginais = tarefasOriginais;
        this.filaPronto = new PriorityQueue<>(Comparator.comparingInt(InstanciaTarefa::getDeadlineAbsoluto));
    }

    public double calcularUtilizacao(List<Tarefa> tarefas) {
        double utilizacao = 0;
        for (Tarefa t : tarefas) {
            utilizacao += (double) t.getCt() / t.getPeriodo();
        }
        return utilizacao;
    }

    public boolean testeEscalonabilidade(List<Tarefa> tarefas) {
        double utilizacao = calcularUtilizacao(tarefas);
        System.out.printf("Utilização = %.2f%n \n", utilizacao);
        return utilizacao <= 1.0;
    }

    @Override
    public void simular(List<Tarefa> listaTarefas, int tempoSim) {
        int instante = 0;

        if (testeEscalonabilidade(tarefasOriginais)) {
            System.out.println("O teste de escalonabilidade foi SATISFATÓRIO\n");
        } else {
            System.out.println("O teste de escalonabilidade é INSUFICIENTE\n");
        }

        do {
            //Libera instâncias das tarefas periodicamente
            for (Tarefa t : tarefasOriginais) {
                if ((instante - t.getOffset()) % t.getPeriodo() == 0 && instante >= t.getOffset()) {
                    InstanciaTarefa nova = new InstanciaTarefa(t, instante);
                    filaPronto.add(nova);

                    ativacoes.put(t.getId(), ativacoes.getOrDefault(t.getId(), 0) + 1);

                    System.out.println("Instante " + instante + ": Tarefa ID " + nova.getIdInstancia() + " liberada.");

                    //Verifica inversão de prioridade (EDF)
                    if (!processador.isEmpty()) {
                        InstanciaTarefa emExecucao = processador.get(0);
                        if (nova.getDeadlineAbsoluto() < emExecucao.getDeadlineAbsoluto()) {
                            System.out.printf("Inversão de prioridade no instante %d: Tarefa ID %d (DL=%d) ficou esperando enquanto ID %d (DL=%d) executava%n",
                                    instante, nova.getIdInstancia(), nova.getDeadlineAbsoluto(),
                                    emExecucao.getIdInstancia(), emExecucao.getDeadlineAbsoluto());
                        }
                    }
                }
            }

            //Detectar perdas de deadline
            List<InstanciaTarefa> perdidas = new ArrayList<>();
            for (InstanciaTarefa inst : filaPronto) {
                if (inst.getDeadlineAbsoluto() <= instante) {
                    System.out.printf("Instante %d: Tarefa ID %d perdeu o deadline (DL=%d)%n",
                            instante, inst.getIdInstancia(), inst.getDeadlineAbsoluto());

                    int idOriginal = inst.getTarefaOriginalId();
                    perdasDeadline.put(idOriginal, perdasDeadline.getOrDefault(idOriginal, 0) + 1);

                    perdidas.add(inst);
                }
            }
            filaPronto.removeAll(perdidas);

            //Preempção
            if (!processador.isEmpty()) {
                InstanciaTarefa atual = processador.get(0);
                InstanciaTarefa maisUrgente = filaPronto.peek();
                if (maisUrgente != null && maisUrgente.getDeadlineAbsoluto() < atual.getDeadlineAbsoluto()) {
                    processador.remove(0);
                    filaPronto.add(atual);
                    InstanciaTarefa novo = filaPronto.poll();
                    processador.add(novo);

                    System.out.println("Instante " + instante + ": Preempção. Tarefa ID " + atual.getIdInstancia() +
                            " saiu. Tarefa ID " + novo.getIdInstancia() + " entrou no processador.");
                }
            }

            //Se processador vazio, coloca a mais urgente
            if (processador.isEmpty() && !filaPronto.isEmpty()) {
                InstanciaTarefa nova = filaPronto.poll();
                processador.add(nova);
                System.out.println("Instante " + instante + ": Tarefa ID " + nova.getIdInstancia() + " entrou no processador.");
            }

            //Processando
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
        
        exibirPerdasDeadline();
    }    
    
    public void exibirPerdasDeadline() {
        System.out.println("\n=== Análise de Perdas de Deadline ===");
        for (Tarefa t : tarefasOriginais) {
            int id = t.getId();
            int total = ativacoes.getOrDefault(id, 0);
            int perdas = perdasDeadline.getOrDefault(id, 0);
            double frequencia = total > 0 ? (double) perdas / total : 0.0;

            System.out.printf("Tarefa ID %d: %d perdas de %d ativações (Frequência = %.2f)%n",
                    id, perdas, total, frequencia);
        }
    }

    @Override
    public void mostraFilaPronto() {
        List<InstanciaTarefa> lista = new ArrayList<>(filaPronto);
        lista.sort(Comparator.comparingInt(InstanciaTarefa::getDeadlineAbsoluto));

        if (lista.isEmpty()) {
            System.out.println("Fila de Pronto: [vazia]\n");
        } else {
            System.out.print("Fila de Pronto: [");
            for (InstanciaTarefa t : lista) {
                System.out.print("ID= " + t.getIdInstancia() +
                        "(CT=" + t.getCtRestante() +
                        ", DeadLine absoluto=" + t.getDeadlineAbsoluto() +
                        ", Periodo=" + t.getTarefa().getPeriodo() + ") ");
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
