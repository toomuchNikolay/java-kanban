package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldTasksEqualsWithSameId() {
        Task task1 = new Task("test task1 title", "test task1 description");
        Task savedTask1 = taskManager.addTask(task1);
        Task task2 = new Task("test task2 title", "test task2 description");
        Task savedTask2 = taskManager.addTask(task2);
        savedTask2.setId(savedTask1.getId());
        taskManager.updateTask(savedTask2);

        assertEquals(savedTask1, savedTask2, "Задачи не равны друг другу");
        assertEquals(savedTask1.hashCode(), savedTask2.hashCode(), "Задачи не равны друг другу");
    }
}