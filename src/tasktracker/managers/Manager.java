package tasktracker.managers;

import tasktracker.interfaces.*;

import java.nio.file.Path;

public final class Manager {
    private Manager() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getDefault(Path path) {
        return FileBackedTaskManager.loadFromFile(path.toFile());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}