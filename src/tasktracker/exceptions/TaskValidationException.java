package tasktracker.exceptions;

public class TaskValidationException extends RuntimeException {
    public TaskValidationException() {
        super("Время недоступно, задача пересекается с существующими");
    }
}