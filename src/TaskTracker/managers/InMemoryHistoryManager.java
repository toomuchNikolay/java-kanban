package TaskTracker.managers;

import TaskTracker.interfaces.HistoryManager;
import TaskTracker.storage.*;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyList;

    public InMemoryHistoryManager() {
        this.historyList = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (historyList.size() >= SIZE_HISTORY_LIST)
            historyList.removeFirst();
        historyList.add(task.getTaskForHistory());
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(historyList);
    }
}