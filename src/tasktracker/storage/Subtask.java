package tasktracker.storage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int minutesDuration, String startDateTime, int epicId) {
        super(title, description, minutesDuration, startDateTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, StatusTask status, Duration duration, LocalDateTime startTime, int epicId) {
        super(id, title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask getTaskForHistory() {
        return new Subtask(this.getId(), this.getTitle(), this.getDescription(), this.getStatus(), this.getDuration(), this.getStartTime(), this.getEpicId());
    }

    public int getEpicId() {
        return epicId;
    }

    public TypeTask getType() {
        return TypeTask.SUBTASK;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id)
                .append(",").append(TypeTask.SUBTASK)
                .append(",").append(title)
                .append(",").append(status)
                .append(",").append(description);
        if (duration != null && startTime != null)
            builder.append(",").append(duration.toMinutes())
                    .append(",").append(startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        builder.append(",").append(epicId);
        return builder.toString();
    }
}