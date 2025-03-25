package tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.exceptions.NotFoundException;
import tasktracker.exceptions.TaskValidationException;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.*;

import java.time.format.DateTimeFormatter;

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
        task = new Task("test task title", "test task description", 60, "01.01.2025 00:00");
        manager.addTask(task);
        epic = new Epic("test epic title", "test epic description");
        manager.addEpic(epic);
        subtask1 = new Subtask("test subtask1 title", "test subtask1 description",
                60, "01.01.2025 10:00", epic.getId());
        manager.addSubtask(subtask1);
        subtask2 = new Subtask("test subtask2 title", "test subtask2 description",
                60, "01.01.2025 11:00", epic.getId());
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
        assertEquals(1, manager.getTaskList().size(), "Null добавился как задача в трекер");
    }

    @Test
    void shouldAddTask() {
        assertNotNull(manager.getTaskList(), "Задача не добавилась в трекер задач");
    }

    @Test
    void shouldReturnTaskById() {
        assertEquals(task, manager.getTask(task.getId()), "Задача по id не найдена");
    }

    @Test
    void shouldDeleteTaskById() {
        manager.removeTask(task.getId());

        assertFalse(manager.getTaskList().contains(task), "Задача не удалена");
    }

    @Test
    void shouldRemoveAllTasks() {
        manager.clearTaskList();

        assertTrue(manager.getTaskList().isEmpty(), "Список задач не пуст");
    }

    @Test
    void shouldNotAddNullAsEpic() {
        manager.addEpic(null);
        assertEquals(1, manager.getEpicList().size(), "Null добавился как эпик в трекер");
    }

    @Test
    void shouldAddEpic() {
        assertNotNull(manager.getTaskList(), "Эпик не добавился в трекер задач");
    }

    @Test
    void shouldReturnEpicById() {
        assertEquals(epic, manager.getEpic(epic.getId()), "Эпик по id не найден");
    }

    @Test
    void shouldDeleteEpicByIdAndSubtasksWithSameEpicId() {
        manager.removeEpic(epic.getId());

        assertFalse(manager.getEpicList().contains(epic), "Эпик не удален");
        assertTrue(manager.getSubtaskList().isEmpty(), "Привязанные подзадачи к эпику не удалены");
    }

    @Test
    void shouldRemoveAllEpicsAndAllSubtasks() {
        manager.clearEpicList();

        assertTrue(manager.getEpicList().isEmpty(), "Список эпиков не пуст");
        assertTrue(manager.getSubtaskList().isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldNotAddNullAsSubtask() {
        manager.addSubtask(null);
        assertEquals(2, manager.getSubtaskList().size(), "Null добавился как подзадача в трекер");
    }

    @Test
    void shouldNotAddSubtaskWithFalseEpicId() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            Subtask subtask = new Subtask("test subtask title", "test subtask description", 111);
            manager.addSubtask(subtask);
        });
        assertEquals("В списке отсутствует эпик с указанным id", exception.getMessage(),
                "Не сработало исключение при проверке доступности времени");
    }

    @Test
    void shouldAddSubtask() {
        assertNotNull(manager.getSubtaskList(), "Подзадача не добавилась в трекер задач");
    }

    @Test
    void shouldAssignedEpicId() {
        assertEquals(2, subtask1.getEpicId(), "У подзадачи отсутствует связанный эпик");
    }

    @Test
    void shouldReturnSubtaskById() {
        assertEquals(subtask2, manager.getSubtask(subtask2.getId()), "Подзадача по id не найдена");
    }

    @Test
    void shouldDeleteSubtaskByIdAndRemoveFromSubtasksIds() {
        manager.removeSubtask(subtask1.getId());

        assertFalse(manager.getSubtaskList().contains(subtask1), "Подзадача не удалена");
        assertFalse(manager.getEpic(subtask1.getEpicId()).getSubtasksIds().contains(subtask1.getId()),
                "Подзадача не удалена из списка привязанных к эпику подзадач");
    }

    @Test
    void shouldRemoveAllSubtasksAndClearSubtasksIdsEpics() {
        manager.clearSubtaskList();

        assertTrue(manager.getSubtaskList().isEmpty(), "Список подзадач не пуст");
        assertTrue(manager.getEpic(epic.getId()).getSubtasksIds().isEmpty(),
                "Список привязанных подзадач к эпику не пуст");
    }

    @Test
    void shouldStatusEpicSetCorrectly() {
        assertEquals(StatusTask.NEW, epic.getStatus(),
                "Статусы привязанных subtask = NEW, статус Epic != NEW");

        subtask1.setStatus(StatusTask.DONE);
        manager.updateSubtask(subtask1);

        assertEquals(StatusTask.IN_PROGRESS, epic.getStatus(),
                "Статусы привязанных subtask = NEW или DONE, статус Epic != IN_PROGRESS");

        subtask2.setStatus(StatusTask.DONE);
        manager.updateSubtask(subtask2);

        assertEquals(StatusTask.DONE, epic.getStatus(),
                "Статусы привязанных subtask = DONE, статус Epic != DONE");
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

    @Test
    void shouldAddToPrioritizedTasks() {
        assertTrue(manager.getPrioritizedTasks().contains(task), "Task не добавился в список приоритетов");
        assertTrue(manager.getPrioritizedTasks().contains(subtask2), "Subtask не добавился в список приоритетов");
        assertFalse(manager.getPrioritizedTasks().contains(epic), "Epic добавился в список приоритетов");
    }

    @Test
    void shouldCheckAvailabilityTime() {
        TaskValidationException exception = assertThrows(TaskValidationException.class, () -> {
            Task checkTask = new Task("check title", "check description", 30, "01.01.2025 10:30");
            manager.addTask(checkTask);
        });
        assertEquals("Время недоступно, задача пересекается с существующими", exception.getMessage(),
                "Не сработало исключение при проверке доступности времени");
    }

    @Test
    void shouldCalculateDurationEpic() {
        assertEquals(120, epic.getDuration().toMinutes(), "Ошибка при расчете периода выполнения эпика");
    }

    @Test
    void shouldCalculateStartTimeEpic() {
        assertEquals("01.01.2025 10:00", epic.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                "Ошибка при расчете даты и времени старта выполнения эпика");
    }

    @Test
    void shouldCalculateEndTimeEpic() {
        assertEquals("01.01.2025 12:00", epic.getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                "Ошибка при расчете даты и времени завершения выполнения эпика");
    }
}