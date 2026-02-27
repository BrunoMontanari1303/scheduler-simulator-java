package classes;

public class InstanciaTarefa {
	private static int contadorInstancias = 0;
	private Tarefa tarefa;
	private int ctRestante;
	private int idInstancia;
	private int deadlineAbsoluto;
	private int instanteChegada;
	private int instanteConclusao;
	private int computationTime;

	public InstanciaTarefa(Tarefa t, int instanteLiberacao) {
		this.tarefa = t;
	    this.ctRestante = t.getCt();
	    this.computationTime = t.getCt();
	    this.deadlineAbsoluto = instanteLiberacao + t.getDeadline();
	    this.idInstancia = ++contadorInstancias;
	    this.instanteChegada = instanteLiberacao;
	}
	
	public void processa() {
		ctRestante--;
	}

	public boolean concluida() {
		return ctRestante <= 0;
	}

	public int getCtRestante() {
		return ctRestante;
	}

	public Tarefa getTarefa() {
		return tarefa;
	}
	 
	public int getIdInstancia() {
		return idInstancia;
	}
	
	public int getDeadlineAbsoluto() {
	    return deadlineAbsoluto;
	}
	
	public int getConclusao() {
	    return instanteConclusao;
	}
	
	public void setConclusao(int instante) {
	    this.instanteConclusao = instante;
	}

	public int getTurnaroundTime() {
	    return instanteConclusao - instanteChegada;
	}

	public int getTarefaOriginalId() {
	    return tarefa.getId();
	}
	
	public int getInstanteChegada() {
	    return instanteChegada;
	}
	
	public int getComputationTime() {
        return computationTime;
    }
}