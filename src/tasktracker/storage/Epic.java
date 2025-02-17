package tasktracker.storage;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksIds = new ArrayList<>();
    }

    private Epic(int id, String title, String description, Status status, List<Integer> subtasksIds) {
        super(id, title, description, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic getTaskForHistory() {
        return new Epic(this.getId(), this.getTitle(), this.getDescription(), this.status, this.getSubtasksIds());
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setStatus(List<Subtask> subtasks) {
        if (subtasks.isEmpty())
            setStatus(Status.NEW);
        else {
            boolean allStatusNew = true;
            boolean allStatusDone = true;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != Status.NEW) {
                    allStatusNew = false;
                }
                if (subtask.getStatus() != Status.DONE) {
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
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}