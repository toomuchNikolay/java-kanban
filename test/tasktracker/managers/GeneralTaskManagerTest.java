package tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.Epic;
import tasktracker.storage.StatusTask;
import tasktracker.storage.Subtask;
import tasktracker.storage.Task;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class GeneralTaskManagerTest<T extends TaskManager> {
    protected T manager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask1;
    protected Subtask subtask2;

    protected abstract T getTaskManager();

    @BeforeEach
    void beforeEach() {
        manager = getTaskManager();
        task = new Task("test task title", "test task description");
        manager.addTask(task);
        epic = new Epic("test epic title", "test epic description");
        manager.addEpic(epic);
        subtask1 = new Subtask("test subtask1 title", "test subtask1 description", epic.getId());
        manager.addSubtask(subtask1);
        subtask2 = new Subtask("test subtask2 title", "test subtask2 description", epic.getId());
        manager.addSubtask(subtask2);
    }

    @Test
    void shouldIncrementCountByOneAfterAddAnyTypeTask() {
        assertEquals(1, task.getId(), "Присвоился неверный id задаче");
        assertEquals(2, epic.getId(), "Присвоился неверный id эпику");
        assertEquals(3, subtask1.getId(), "Присвоился неверный id подзадаче");
    }

    @Test
    void shouldNotAddNullAsTask() {
        manager.addTask(null);
        assertEquals(1, manager.getAllTask().size(), "Null добавился как задача в трекер");
    }

    @Test
    void shouldAddTask() {
        assertNotNull(manager.getAllTask(), "Задача не добавилась в трекер задач");
    }

    @Test
    void shouldReturnTaskById() {
        assertEquals(task, manager.getTask(task.getId()), "Задача по id не найдена");
    }

    @Test
    void shouldDeleteTaskById() {
        manager.removeTask(task.getId());

        assertFalse(manager.getAllTask().contains(task), "Задача не удалена");
    }

    @Test
    void shouldRemoveAllTasks() {
        manager.removeAllTask();

        assertTrue(manager.getAllTask().isEmpty(), "Список задач не пуст");
    }

    @Test
    void shouldNotAddNullAsEpic() {
        manager.addEpic(null);
        assertEquals(1, manager.getAllEpic().size(), "Null добавился как эпик в трекер");
    }

    @Test
    void shouldAddEpic() {
        assertNotNull(manager.getAllTask(), "Эпик не добавился в трекер задач");
    }

    @Test
    void shouldReturnEpicById() {
        assertEquals(epic, manager.getEpic(epic.getId()), "Эпик по id не найден");
    }

    @Test
    void shouldDeleteEpicByIdAndSubtasksWithSameEpicId() {
        manager.removeEpic(epic.getId());

        assertFalse(manager.getAllEpic().contains(epic), "Эпик не удален");
        assertTrue(manager.getAllSubtask().isEmpty(), "Привязанные подзадачи к эпику не удалены");
    }

    @Test
    void shouldRemoveAllEpicsAndAllSubtasks() {
        manager.removeAllEpic();

        assertTrue(manager.getAllEpic().isEmpty(), "Список эпиков не пуст");
        assertTrue(manager.getAllSubtask().isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldNotAddNullAsSubtask() {
        manager.addSubtask(null);
        assertEquals(2, manager.getAllSubtask().size(), "Null добавился как подзадача в трекер");
    }

    @Test
    void shouldNotAddSubtaskWithFalseEpicId() {
        Subtask testSubtask = new Subtask("test subtask title", "test subtask description", 111);
        manager.addSubtask(testSubtask);

        assertFalse(manager.getAllSubtask().contains(testSubtask),
                "Подзадача с несуществующим epicId добавилась в трекер задач");
    }

    @Test
    void shouldAddSubtask() {
        assertNotNull(manager.getAllSubtask(), "Подзадача не добавилась в трекер задач");
    }

    @Test
    void shouldReturnSubtaskById() {
        assertEquals(subtask2, manager.getSubtask(subtask2.getId()), "Подзадача по id не найдена");
    }

    @Test
    void shouldDeleteSubtaskByIdAndRemoveFromSubtasksIds() {
        manager.removeSubtask(subtask1.getId());
        manager.removeSubtask(111);

        assertFalse(manager.getAllSubtask().contains(subtask1), "Подзадача не удалена");
        assertFalse(manager.getEpic(subtask1.getEpicId()).getSubtasksIds().contains(subtask1.getId()),
                "Подзадача не удалена из списка привязанных к эпику подзадач");
    }

    @Test
    void shouldRemoveAllSubtasksAndClearSubtasksIdsEpics() {
        manager.removeAllSubtask();

        assertTrue(manager.getAllSubtask().isEmpty(), "Список подзадач не пуст");
        assertTrue(manager.getEpic(epic.getId()).getSubtasksIds().isEmpty(),
                "Список привязанных подзадач к эпику не пуст");
    }

    @Test
    void shouldStatusEpicSetCorrectly() {
        assertEquals(StatusTask.NEW, epic.getStatus(), "Неверный статус эпика");

        subtask1.setStatus(StatusTask.DONE);
        manager.updateSubtask(subtask1);

        assertEquals(StatusTask.IN_PROGRESS, epic.getStatus(), "Некорректно изменился статус эпика");

        subtask2.setStatus(StatusTask.DONE);
        manager.updateSubtask(subtask2);

        assertEquals(StatusTask.DONE, epic.getStatus(), "Некорректно изменился итоговый статус эпика");
    }

    @Test
    void shouldImmutabilityTaskWhenAddToManager() {
        Task addedTask = manager.getTask(task.getId());

        assertEquals(task.getTitle(), addedTask.getTitle(), "Название задачи изменилось");
        assertEquals(task.getDescription(), addedTask.getDescription(), "Описание задачи изменилось");
        assertEquals(task.getId(), addedTask.getId(), "Идентификатор задачи изменился");
        assertEquals(task.getStatus(), addedTask.getStatus(), "Статус задач изменился");
    }

    @Test
    void shouldAddToHistoryWhenGetTasks() {
        manager.getTask(task.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());

        assertEquals(3, manager.getHistory().size(), "В историю просмотров не добавились задачи");
    }
}