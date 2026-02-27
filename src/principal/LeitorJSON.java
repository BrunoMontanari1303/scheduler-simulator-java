package principal;
import org.json.JSONArray;
import org.json.JSONObject;
import classes.Tarefa;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LeitorJSON {
    private int tempoSimulacao;
    private String scheduler;
    private List<Tarefa> tarefas;

    public void lerArquivo(String caminho) {
        try {
            String conteudo = new String(Files.readAllBytes(Paths.get(caminho)));

            JSONObject json = new JSONObject(conteudo);

            tempoSimulacao = json.getInt("simulation_time");
            scheduler = json.getString("scheduler_name");

            JSONArray tasksArray = json.getJSONArray("tasks");
            tarefas = new ArrayList<>();

            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject obj = tasksArray.getJSONObject(i);

                Tarefa tarefa = new Tarefa(i + 1, obj.getInt("offset"), obj.getInt("computation_time"), obj.getInt("period_time"), obj.getInt("quantum"), obj.getInt("deadline"));
                tarefas.add(tarefa);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTempoSimulacao() {
        return tempoSimulacao;
    }

    public String getScheduler() {
        return scheduler;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }
}
