package classes;

import java.util.List;

public interface Escalonador {
	void simular(List<Tarefa> tarefas, int tempoSim);
	void mostraFilaPronto();
	void calcularTAT();
	void calcularWT();
	void verificarStarvation();
}
