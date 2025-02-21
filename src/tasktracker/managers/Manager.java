package tasktracker.managers;

import tasktracker.interfaces.*;

import java.nio.file.Path;

public final class Manager {
    private Manager() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBacked(Path path) {
        return new FileBackedTaskManager(path);
    }
}