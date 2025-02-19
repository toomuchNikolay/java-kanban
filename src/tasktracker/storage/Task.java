package tasktracker.storage;

import java.util.Objects;

public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected StatusTask status;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = StatusTask.NEW;
    }

    public Task(int id, String title, String description, StatusTask status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task getTaskForHistory() {
        return new Task(this.getId(), this.getTitle(), this.getDescription(), this.getStatus());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StatusTask getStatus() {
        return status;
    }

    public void setStatus(StatusTask status) {
        this.status = status;
    }

    public TypeTask getType() {
        return TypeTask.TASK;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s", id, TypeTask.TASK, title, status.toString(), description);
    }
}