package TaskTracker.managers;

import TaskTracker.interfaces.*;
import TaskTracker.storage.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final TaskManager taskManager = Manager.getDefault();
    private final HistoryManager historyManager = Manager.getDefaultHistory();

    @Test
    void shouldReturnPreviousVersionTasks() {
        Task task = new Task("test task title", "test task desc");
        Task savedTask = taskManager.addTask(task);

        taskManager.getTask(savedTask.getId());
        savedTask.setTitle("test savedTask title");
        taskManager.updateTask(savedTask);
        taskManager.getTask(savedTask.getId());

        ArrayList<Task> history1 = historyManager.getHistory();

        assertEquals(2, history1.size());
        assertEquals("test task title", history1.get(0).getTitle());
        assertEquals("test savedTask title", history1.get(1).getTitle());
    }
}