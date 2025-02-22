package tasktracker.storage;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, StatusTask status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask getTaskForHistory() {
        return new Subtask(this.getId(), this.getTitle(), this.getDescription(), this.status, this.getEpicId());
    }

    public int getEpicId() {
        return epicId;
    }

    public TypeTask getType() {
        return TypeTask.SUBTASK;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", id, TypeTask.SUBTASK, title, status.toString(), description, epicId);
    }
}