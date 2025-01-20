package TaskTracker.managers;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.storage.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private final TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldBePositiveWhenAddTasksAllTypes() {
        Task task = new Task("test task title", "test task desc");
        Task savedTask = taskManager.addTask(task);
        Epic epic = new Epic("test epic title", "test epic desc");
        Epic savedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("test subtask title", "test subtask desc", epic.getId());
        Subtask savedSubtask = taskManager.addSubtask(subtask);

        assertNotNull(savedTask, "Task не найден");
        assertNotNull(savedEpic, "Epic не найден");
        assertNotNull(savedSubtask, "Subtask не найден");

        assertEquals(task, savedTask, "Task не совпадают");
        assertEquals(epic, savedEpic, "Epic не совпадают");
        assertEquals(subtask, savedSubtask, "Subtask не совпадают");

        final ArrayList<Task> tasks = taskManager.getAllTask();
        final ArrayList<Epic> epics = taskManager.getAllEpic();
        final ArrayList<Subtask> subtasks = taskManager.getAllSubtask();

        assertNotNull(tasks, "Tasks не возвращаются");
        assertNotNull(epics, "Epics не возвращаются");
        assertNotNull(subtasks, "Subtasks не возвращаются");

//        assertEquals(1, tasks.size(), "Неверное количество Tasks");
//        assertEquals(1, epics.size(), "Неверное количество Epics");
//        assertEquals(1, subtasks.size(), "Неверное количество Subtasks");

        Task foundTask = taskManager.getTask(savedTask.getId());
        Epic foundEpic = taskManager.getEpic(savedEpic.getId());
        Subtask foundSubtask = taskManager.getSubtask(savedSubtask.getId());

        assertNotNull(foundTask, "Task по идентификатору не найден");
        assertNotNull(foundEpic, "Epic по идентификатору не найден");
        assertNotNull(foundSubtask, "Subtask по идентификатору не найден");

        assertEquals(foundTask, task, "Task не совпадают");
        assertEquals(foundEpic, epic, "Epic не совпадают");
        assertEquals(foundSubtask, subtask, "Epic не совпадают");
    }

    @Test
    void shouldBePositiveWhenSetIdTaskManualAndGeneration() {
        Task task1 = new Task("test task1 title", "test task1 desc");
        Task task2 = new Task("test task1 title", "test task1 desc");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        task2.setId(task1.getId());
        taskManager.updateTask(task2);

        final ArrayList<Task> tasks1 = taskManager.getAllTask();

        assertEquals(2, tasks1.size(), "Неверное количество задач");
    }

    @Test
    void shouldReturnStatusInProgressForEpic() {
        Epic epic1 = new Epic("test epic title", "test epic desc");
        Epic savedEpic1 = taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("test subtask1 title", "test subtask1 desc", epic1.getId());
        Subtask savedSubtask1 = taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("test subtask2 title", "test subtask2 desc", epic1.getId());
        Subtask savedSubtask2 = taskManager.addSubtask(subtask2);

        assertEquals(savedEpic1.getStatus(), Status.NEW, "Неверный статус Epic");

        savedSubtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(savedSubtask1);

        assertEquals(savedEpic1.getStatus(), Status.IN_PROGRESS, "Некорректно изменился статус Epic");

        savedSubtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(savedSubtask2);

        assertEquals(savedEpic1.getStatus(), Status.DONE, "Некорректно изменился статус Epic");
    }
}