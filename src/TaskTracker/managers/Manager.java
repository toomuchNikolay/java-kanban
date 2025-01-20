package TaskTracker.managers;

import TaskTracker.interfaces.*;

public class Manager {

    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    public static TaskManager getDefault() {
        if (taskManager == null)
            taskManager = new InMemoryTaskManager(getDefaultHistory());
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        if (historyManager == null)
            historyManager = new InMemoryHistoryManager();
        return historyManager;
    }
}