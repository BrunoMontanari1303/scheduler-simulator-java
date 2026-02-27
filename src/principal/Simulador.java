package principal;

import classes.*;
import java.util.List;

public class Simulador {
	public static void main(String args[]) {
		LeitorJSON leitor = new LeitorJSON();
		//Mudar para o caminho onde o JSON esta armazenado
        leitor.lerArquivo("C:/Users/bruno/eclipse-workspace/TrabalhoEscalonador/data/Tarefas.json");

        List<Tarefa> tarefas = leitor.getTarefas();
        int tempoSimulacao = leitor.getTempoSimulacao();
        String schedulerNome = leitor.getScheduler().toUpperCase();

        Escalonador escalonador = null;

        switch (schedulerNome) {
            case "FCFS":
                escalonador = new FCFS(tarefas);
                break;
            case "SJF":
                escalonador = new SJF(tarefas);
                break;
            case "RR":
            	escalonador = new RR(tarefas);
            	break;
            case "SRTF":
            	escalonador = new SRTF(tarefas);          	
            	break;
            case "RM":
            	escalonador = new RM(tarefas);
            	
            	break;
            case "EDF":
            	escalonador = new EDF(tarefas);
            	break;
            default:
                System.out.println("Algoritmo " + schedulerNome + " não reconhecido.");
                return;
        }

        System.out.println("=== Iniciando simulação com algoritmo " + schedulerNome + " ===");
        escalonador.simular(tarefas, tempoSimulacao);
        System.out.println("-----------------------------------------");
        escalonador.calcularTAT();
        escalonador.calcularWT();
        escalonador.verificarStarvation();
        
    }	
}
