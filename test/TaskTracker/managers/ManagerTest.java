package TaskTracker.managers;

import TaskTracker.interfaces.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagerTest {
    TaskManager taskManager1 = Manager.getDefault();
    TaskManager taskManager2 = Manager.getDefault();
    HistoryManager historyManager1 = Manager.getDefaultHistory();
    HistoryManager historyManager2 = Manager.getDefaultHistory();

    @Test
    void shouldReturnInitializedTaskManager() {
        assertNotNull(taskManager1, "Метод getDefault не создает новый объект");
        assertEquals(taskManager1, taskManager2, "Метод getDefault возвращает разные объекты");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        assertNotNull(historyManager1, "Метод getDefaultHistory не создает новый объект");
        assertEquals(historyManager1, historyManager2, "Метод getDefaultHistory возвращает разные объекты");
    }
}