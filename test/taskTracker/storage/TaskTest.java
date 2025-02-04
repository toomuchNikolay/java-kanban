package taskTracker.storage;

import taskTracker.interfaces.TaskManager;
import taskTracker.managers.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private final TaskManager taskManager = Manager.getDefault();
    private Task savedTask1;

    @BeforeEach
    void beforeEach() {
        Task task1 = new Task("test task1 title", "test task1 description");
        savedTask1 = taskManager.addTask(task1);
    }

    @Test
    void shouldNotEqualsNull() {
        assertNotEquals(savedTask1, null, "Задача и null равны");
    }

    @Test
    void shouldNotEqualsAnotherClass() {
        Epic epic = new Epic("test epic title", "test epic description");
        Epic savedEpic = taskManager.addEpic(epic);
        savedEpic.setId(savedTask1.getId());
        taskManager.updateEpic(savedEpic);

        assertNotEquals(savedTask1, savedEpic, "Задачи разного типа равны");
    }

    @Test
    void shouldTasksEqualsWithSameId() {
        Task task2 = new Task("test task2 title", "test task2 description");
        Task savedTask2 = taskManager.addTask(task2);
        savedTask2.setId(savedTask1.getId());
        taskManager.updateTask(savedTask2);

        assertEquals(savedTask1, savedTask2, "Задачи не равны друг другу");
        assertEquals(savedTask1.hashCode(), savedTask2.hashCode(), "Задачи не равны друг другу");
    }
}