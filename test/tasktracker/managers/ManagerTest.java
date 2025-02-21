package tasktracker.managers;

import tasktracker.interfaces.*;
import tasktracker.storage.Task;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagerTest {
    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager taskManager = Manager.getDefault();

        assertNotNull(taskManager, "Экземпляр TaskManager не проинициализирован");

        Task task = new Task("test task title", "test task description");
        taskManager.addTask(task);
        List<Task> tasks = taskManager.getAllTask();

        assertEquals(1, tasks.size(), "TaskManager не добавил задачу в список");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Manager.getDefaultHistory();

        assertNotNull(historyManager, "Экземпляр HistoryManager не проинициализирован");

        Task task = new Task("test task title", "test task description");
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "HistoryManager не добавил задачу в историю");
    }

    @Test
    void shouldReturnInitializedFileBackedTaskManager() throws IOException {
        File tempFile = File.createTempFile("temp", ".scv");
        TaskManager backedManager = Manager.getFileBacked(tempFile.toPath());

        assertNotNull(backedManager, "Экземпляр FileBackedTaskManager не проинициализирован");

        Task task = new Task("test task title", "test task description");
        backedManager.addTask(task);

        BufferedReader br = new BufferedReader(new FileReader(tempFile));
        String line = null;
        while (br.ready()) {
            line = br.readLine();
        }
        assertEquals("1,TASK,test task title,NEW,test task description", line,
                "Задача в файл не сохранилась");
    }
}