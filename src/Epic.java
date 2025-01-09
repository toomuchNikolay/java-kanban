import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasksToEpic;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksToEpic = new ArrayList<>();
    }

    public void addSubtaskToEpic(Subtask subtask) {
        subtasksToEpic.add(subtask);
    }

    public void updateStatus(ArrayList<Subtask> subtasks) {
        if (subtasks.isEmpty())
            setStatus(Status.NEW);
        else {
            boolean allStatusNew = true;
            boolean allStatusDone = true;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != Status.NEW) {
                    allStatusNew = false;
                } if (subtask.getStatus() != Status.DONE) {
                    allStatusDone = false;
                }
            }
            if (allStatusNew)
                setStatus(Status.NEW);
            else if (allStatusDone)
                setStatus(Status.DONE);
            else
                setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtasks=" + subtasksToEpic +
                '}';
    }
}