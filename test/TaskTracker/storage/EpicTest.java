package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private final TaskManager taskManager = Manager.getDefault();
    private Epic savedEpic1;

    @BeforeEach
    void beforeEach() {
        Epic epic1 = new Epic("test epic1 title", "test epic1 description");
        savedEpic1 = taskManager.addEpic(epic1);
    }

    @Test
    void shouldNotEqualsNull() {
        assertNotEquals(savedEpic1, null, "Эпик и null равны");
    }

    @Test
    void shouldNotEqualsAnotherClass() {
        Task task = new Task("test task title", "test task description");
        Task savedTask = taskManager.addTask(task);
        savedTask.setId(savedEpic1.getId());
        taskManager.updateTask(savedTask);

        assertNotEquals(savedEpic1, savedTask, "Задачи разного типа равны");
    }

    @Test
    void shouldEpicsEqualsWithSameId() {
        Epic epic2 = new Epic("test epic2 title", "test epic2 description");
        Epic savedEpic2 = taskManager.addEpic(epic2);
        savedEpic2.setId(savedEpic1.getId());
        taskManager.updateEpic(savedEpic2);

        assertEquals(savedEpic1, savedEpic2, "Эпики не равны друг другу");
    }
}