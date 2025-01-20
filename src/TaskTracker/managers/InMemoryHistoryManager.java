package TaskTracker.managers;

import TaskTracker.interfaces.HistoryManager;
import TaskTracker.storage.*;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int SIZE_HISTORY_LIST = 10;
    private final ArrayList<Task> historyList;

    public InMemoryHistoryManager() {
        this.historyList = new ArrayList<>(SIZE_HISTORY_LIST);
    }

    @Override
    public void add(Task task) {
        if (historyList.size() >= SIZE_HISTORY_LIST)
            historyList.removeFirst();
        historyList.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyList);
    }
}