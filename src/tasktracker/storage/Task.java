package tasktracker.storage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected StatusTask status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = StatusTask.NEW;
    }

    public Task(String title, String description, int minutesDuration, String startDateTime) {
        this.title = title;
        this.description = description;
        this.status = StatusTask.NEW;
        this.duration = Duration.ofMinutes(minutesDuration);
        this.startTime = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public Task(int id, String title, String description, StatusTask status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task getTaskForHistory() {
        return new Task(this.getId(), this.getTitle(), this.getDescription(), this.getStatus(), this.getDuration(), this.getStartTime());
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

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
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
        StringBuilder builder = new StringBuilder();
        builder.append(id)
                .append(",").append(TypeTask.TASK)
                .append(",").append(title)
                .append(",").append(status)
                .append(",").append(description);
        if (duration != null && startTime != null)
            builder.append(",").append(duration.toMinutes())
                    .append(",").append(startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        return builder.toString();
    }
}