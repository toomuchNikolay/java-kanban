package TaskTracker.interfaces;

import TaskTracker.storage.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id);
    List<Task> getHistory();
}