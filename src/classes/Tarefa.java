package classes;

public class Tarefa {
	private int id;
	int offset;
    int ct;
    int quantum;
    int periodo;
    int deadline;

    public Tarefa(int id, int offset, int ct, int periodo,int quantum, int deadline) {
    	this.id = id;
        this.offset = offset;
        this.ct = ct;
        this.quantum = quantum;
        this.periodo = periodo;
        this.deadline = deadline;
    }   
    
    public Tarefa() {
    	
    }
     
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCt() {
        return ct;
    }

    public void setCt(int ct) {
        this.ct = ct;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }
    
    public void processa(){
        this.ct--;
    }      
}
