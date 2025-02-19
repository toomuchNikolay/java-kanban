package tasktracker.managers;

import tasktracker.interfaces.TaskManager;
import tasktracker.storage.*;
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
        assertEquals(3, subtask1.getId(), "Присвоился неверный id подзадаче");
    }

    @Test
    void shouldNotAddNullAsTask() {
        taskManager.addTask(null);
        assertEquals(1, taskManager.getAllTask().size(), "Null добавился как задача в трекер");
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
        taskManager.removeTask(task.getId());

        assertFalse(taskManager.getAllTask().contains(task), "Задача не удалена");
    }

    @Test
    void shouldRemoveAllTasks() {
        taskManager.removeAllTask();

        assertTrue(taskManager.getAllTask().isEmpty(), "Список задач не пуст");
    }

    @Test
    void shouldNotAddNullAsEpic() {
        taskManager.addEpic(null);
        assertEquals(1, taskManager.getAllEpic().size(), "Null добавился как эпик в трекер");
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
        taskManager.removeEpic(epic.getId());

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
    void shouldNotAddNullAsSubtask() {
        taskManager.addSubtask(null);
        assertEquals(2, taskManager.getAllSubtask().size(), "Null добавился как подзадача в трекер");
    }

    @Test
    void shouldNotAddSubtaskWithFalseEpicId() {
        Subtask testSubtask = new Subtask("test subtask title", "test subtask description", 111);
        taskManager.addSubtask(testSubtask);

        assertFalse(taskManager.getAllSubtask().contains(testSubtask),
                "Подзадача с несуществующим epicId добавилась в трекер задач");
    }

    @Test
    void shouldAddSubtask() {
        assertNotNull(taskManager.getAllSubtask(), "Подзадача не добавилась в трекер задач");
    }

    @Test
    void shouldReturnSubtaskById() {
        assertEquals(subtask2, taskManager.getSubtask(subtask2.getId()), "Подзадача по id не найдена");
    }

    @Test
    void shouldDeleteSubtaskByIdAndRemoveFromSubtasksIds() {
        taskManager.removeSubtask(subtask1.getId());
        taskManager.removeSubtask(111);

        assertFalse(taskManager.getAllSubtask().contains(subtask1), "Подзадача не удалена");
        assertFalse(taskManager.getEpic(subtask1.getEpicId()).getSubtasksIds().contains(subtask1.getId()),
                "Подзадача не удалена из списка привязанных к эпику подзадач");
    }

    @Test
    void shouldRemoveAllSubtasksAndClearSubtasksIdsEpics() {
        taskManager.removeAllSubtask();

        assertTrue(taskManager.getAllSubtask().isEmpty(), "Список подзадач не пуст");
        assertTrue(taskManager.getEpic(epic.getId()).getSubtasksIds().isEmpty(),
                "Список привязанных подзадач к эпику не пуст");
    }

    @Test
    void shouldStatusEpicSetCorrectly() {
        assertEquals(StatusTask.NEW, epic.getStatus(), "Неверный статус эпика");

        subtask1.setStatus(StatusTask.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(StatusTask.IN_PROGRESS, epic.getStatus(), "Некорректно изменился статус эпика");

        subtask2.setStatus(StatusTask.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(StatusTask.DONE, epic.getStatus(), "Некорректно изменился итоговый статус эпика");
    }

    @Test
    void shouldImmutabilityTaskWhenAddToManager() {
        Task addedTask = taskManager.getTask(task.getId());

        assertEquals(task.getTitle(), addedTask.getTitle(), "Название задачи изменилось");
        assertEquals(task.getDescription(), addedTask.getDescription(), "Описание задачи изменилось");
        assertEquals(task.getId(), addedTask.getId(), "Идентификатор задачи изменился");
        assertEquals(task.getStatus(), addedTask.getStatus(), "Статус задач изменился");
    }

    @Test
    void shouldAddToHistoryWhenGetTasks() {
        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());

        assertEquals(3, taskManager.getHistory().size(), "В историю просмотров не добавились задачи");
    }
}