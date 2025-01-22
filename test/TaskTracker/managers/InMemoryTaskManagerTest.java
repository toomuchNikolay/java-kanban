package TaskTracker.managers;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.storage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private final TaskManager taskManager = Manager.getDefault();
    private Task task;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void beforeEach() {
        task = new Task("test task title", "test task description");
        taskManager.addTask(task);
        epic = new Epic("test epic title", "test epic description");
        taskManager.addEpic(epic);
        subtask1 = new Subtask("test subtask1 title", "test subtask1 description", epic.getId());
        taskManager.addSubtask(subtask1);
        subtask2 = new Subtask("test subtask2 title", "test subtask2 description", epic.getId());
        taskManager.addSubtask(subtask2);
    }

    @Test
    void shouldIncrementCountByOneAfterAddAnyTypeTask() {
        assertEquals(1, task.getId(), "Присвоился неверный id задаче");
        assertEquals(2, epic.getId(), "Присвоился неверный id эпику");
        assertEquals(4, subtask2.getId(), "Присвоился неверный id подзадаче");
    }

    @Test
    void shouldAddTask() {
        assertNotNull(taskManager.getAllTask(), "Задача не добавилась в трекер задач");
    }

    @Test
    void shouldReturnTaskById() {
        assertEquals(task, taskManager.getTask(task.getId()), "Задача по id не найдена");
    }

    @Test
    void shouldDeleteTaskById() {
        taskManager.deleteTask(task.getId());

        assertFalse(taskManager.getAllTask().contains(task), "Задача не удалена");
    }

    @Test
    void shouldRemoveAllTasks() {
        taskManager.removeAllTask();

        assertTrue(taskManager.getAllTask().isEmpty(), "Список задач не пуст");
    }

    @Test
    void shouldAddEpic() {
        assertNotNull(taskManager.getAllTask(), "Эпик не добавился в трекер задач");
    }

    @Test
    void shouldReturnEpicById() {
        assertEquals(epic, taskManager.getEpic(epic.getId()), "Эпик по id не найден");
    }

    @Test
    void shouldDeleteEpicByIdAndSubtasksWithSameEpicId() {
        taskManager.deleteEpic(epic.getId());

        assertFalse(taskManager.getAllEpic().contains(epic), "Эпик не удален");
        assertTrue(taskManager.getAllSubtask().isEmpty(), "Привязанные подзадачи к эпику не удалены");
    }

    @Test
    void shouldRemoveAllEpicsAndAllSubtasks() {
        taskManager.removeAllEpic();

        assertTrue(taskManager.getAllEpic().isEmpty(), "Список эпиков не пуст");
        assertTrue(taskManager.getAllSubtask().isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldAddSubtask() {
        assertNotNull(taskManager.getAllSubtask(), "Эпик не добавился в трекер задач");
    }

    @Test
    void shouldReturnSubtaskById() {
        assertEquals(subtask2, taskManager.getSubtask(subtask2.getId()), "Эпик по id не найден");
    }

    @Test
    void shouldDeleteSubtaskByIdAndRemoveFromSubtasksIds() {
        taskManager.deleteSubtask(subtask1.getId());

        assertFalse(taskManager.getAllSubtask().contains(subtask1), "Подзадача не удалена");
        assertFalse(taskManager.getEpic(subtask1.getEpicId()).getSubtasksIds().contains(subtask1.getId()));
    }

    @Test
    void shouldRemoveAllSubtaksAndClearSubtasksIdsEpics() {
        taskManager.removeAllSubtask();

        assertTrue(taskManager.getAllSubtask().isEmpty(), "Список подзадач не пуст");
        assertTrue(taskManager.getEpic(epic.getId()).getSubtasksIds().isEmpty(),
                "Список привязанных подзадач к эпику не пуст");
    }

    @Test
    void shouldStatusEpicSetCorrectly() {
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Некорректно изменился статус эпика");

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(), "Некорректно изменился итоговый статус эпика");
    }

    @Test
    void shouldImmutabilityTaskWhenAddToManager() {
        Task addedTask = taskManager.getTask(task.getId());

        assertEquals(task.getTitle(), addedTask.getTitle(), "Название задачи изменилось");
        assertEquals(task.getDescription(), addedTask.getDescription(), "Описание задачи изменилось");
        assertEquals(task.getId(), addedTask.getId(), "Идентификатор задачи изменился");
        assertEquals(task.getStatus(), addedTask.getStatus(), "Статус задач изменился");
    }
//
//    @Test
//    void shouldBePositiveWhenSetIdTaskManualAndGeneration() {
//        Task task1 = new Task("test task1 title", "test task1 desc");
//        Task task2 = new Task("test task1 title", "test task1 desc");
//        taskManager.addTask(task1);
//        taskManager.addTask(task2);
//        task2.setId(task1.getId());
//        taskManager.updateTask(task2);
//
//        final ArrayList<Task> tasks1 = taskManager.getAllTask();
//
//        assertEquals(2, tasks1.size(), "Неверное количество задач");
//    }
}