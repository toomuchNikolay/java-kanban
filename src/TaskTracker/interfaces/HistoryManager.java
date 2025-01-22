package TaskTracker.interfaces;

import TaskTracker.storage.Task;

import java.util.List;

public interface HistoryManager {
    int SIZE_HISTORY_LIST = 10;

    void add(Task task);
    List<Task> getHistory();
}