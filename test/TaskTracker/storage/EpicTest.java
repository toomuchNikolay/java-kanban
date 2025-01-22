package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldEpicsEqualsWithSameId() {
        Epic epic1 = new Epic("test epic1 title", "test epic1 description");
        Epic savedEpic1 = taskManager.addEpic(epic1);
        Epic epic2 = new Epic("test epic2 title", "test epic2 description");
        Epic savedEpic2 = taskManager.addEpic(epic2);
        savedEpic2.setId(savedEpic1.getId());
        taskManager.updateEpic(savedEpic2);

        assertEquals(savedEpic1, savedEpic2, "Эпики не равны друг другу");
        assertEquals(savedEpic1.hashCode(), savedEpic2.hashCode(), "Эпики не равны друг другу");
    }
}