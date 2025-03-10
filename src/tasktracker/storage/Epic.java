package tasktracker.storage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.subtasksIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, StatusTask status, Duration duration, LocalDateTime startTime) {
        super(id, title, description, status, duration, startTime);
        this.subtasksIds = new ArrayList<>();
    }

    public Epic getTaskForHistory() {
        return new Epic(this.getId(), this.getTitle(), this.getDescription(), this.getStatus(), this.getDuration(), this.getStartTime());
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

    public void setDuration(List<Subtask> subtasks) {
        setDuration(subtasks.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus));
    }

    public void setStartTime(List<Subtask> subtasks) {
        subtasks.stream()
                .min(Comparator.comparing(Subtask::getStartTime))
                .ifPresent(sub -> setStartTime(sub.getStartTime()));
    }

    public void setEndTime(List<Subtask> subtasks) {
        endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public TypeTask getType() {
        return TypeTask.EPIC;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id)
                .append(",").append(TypeTask.EPIC)
                .append(",").append(title)
                .append(",").append(status)
                .append(",").append(description);
        if (duration != null && startTime != null)
            builder.append(",").append(duration.toMinutes())
                    .append(",").append(startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        return builder.toString();
    }
}