package tasktracker.managers;

import tasktracker.interfaces.*;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ManagerTest {
    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager taskManager = Manager.getDefault();

        assertNotNull(taskManager, "Экземпляр TaskManager не проинициализирован");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Manager.getDefaultHistory();

        assertNotNull(historyManager, "Экземпляр HistoryManager не проинициализирован");
    }

    @Test
    void shouldReturnInitializedFileBackedTaskManager() throws IOException {
        TaskManager backedManager = Manager.getDefault(File.createTempFile("temp", ".scv").toPath());

        assertNotNull(backedManager, "Экземпляр FileBackedTaskManager не проинициализирован");
    }
}