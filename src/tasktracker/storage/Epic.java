package tasktracker.storage;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, StatusTask status) {
        super(id, title, description, status);
        this.subtasksIds = new ArrayList<>();
    }

    public Epic getTaskForHistory() {
        return new Epic(this.getId(), this.getTitle(), this.getDescription(), this.status);
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setStatus(List<Subtask> subtasks) {
        if (subtasks.isEmpty())
            setStatus(StatusTask.NEW);
        else {
            boolean allStatusNew = true;
            boolean allStatusDone = true;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != StatusTask.NEW) {
                    allStatusNew = false;
                }
                if (subtask.getStatus() != StatusTask.DONE) {
                    allStatusDone = false;
                }
            }
            if (allStatusNew)
                setStatus(StatusTask.NEW);
            else if (allStatusDone)
                setStatus(StatusTask.DONE);
            else
                setStatus(StatusTask.IN_PROGRESS);
        }
    }

    public TypeTask getType() {
        return TypeTask.EPIC;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s", id, TypeTask.EPIC, title, status.toString(), description);
    }
}