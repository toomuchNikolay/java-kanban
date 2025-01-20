package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldBePositiveWhenIdTasksEquals() {
        Task task1 = new Task("test1 title", "test1 desc");
        Task savedTask1 = taskManager.addTask(task1);
        Task task2 = new Task("test2 title", "test2 desc");
        Task savedTask2 = taskManager.addTask(task2);
        savedTask2.setId(savedTask1.getId());

        assertEquals(savedTask1, savedTask2, "Задачи не равны друг другу");
        assertEquals(savedTask1.hashCode(), savedTask2.hashCode(), "Задачи не равны друг другу");
    }

    @Test
    void shouldReturnImmutabilityTaskWhenAdded() {
        Task task = new Task("test task title", "test task desc");
        taskManager.addTask(task);

        Task addedTask = taskManager.getTask(task.getId());

        assertEquals(task.getTitle(), addedTask.getTitle(), "Названия задач не совпадают");
        assertEquals(task.getDescription(), addedTask.getDescription(), "Описания задач не совпадают");
        assertEquals(task.getId(), addedTask.getId(), "Идентификаторы задач не совпадают");
        assertEquals(task.getStatus(), addedTask.getStatus(), "Статусы задач не совпадают");
    }
}