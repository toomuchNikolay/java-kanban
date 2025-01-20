package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldBePositiveWhenIdEpicsEquals() {
        Epic epic1 = new Epic("test1 title", "test1 desc");
        Epic savedEpic1 = taskManager.addEpic(epic1);
        Epic epic2 = new Epic("test2 title", "test2 desc");
        Epic savedEpic2 = taskManager.addEpic(epic2);
        savedEpic2.setId(savedEpic1.getId());

        assertEquals(savedEpic1, savedEpic2, "Задачи не равны друг другу");
        assertEquals(savedEpic1.hashCode(), savedEpic2.hashCode(), "Задачи не равны друг другу");
    }

    @Test
    void shouldReturnNullSubtasksIdsWhenEpicAddToSubtasksIds() {
        Epic epic = new Epic("test title", "test desc");
        Epic savedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("test1 title", "test1 desc", epic.getId());
        subtask.setId(savedEpic.getId());
        taskManager.addSubtask(subtask);

        assertTrue(savedEpic.getSubtasksIds().isEmpty(), "Список привязанных подзадач не пустой");
    }
}