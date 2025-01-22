package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldSubtasksEqualsWithSameId() {
        Epic epic = new Epic("test epic title", "test epic description");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("test subtask1 title", "test subtask1 description", epic.getId());
        Subtask savedSubtask1 = taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("test subtask2 title", "test subtask2 description", epic.getId());
        Subtask savedSubtask2 = taskManager.addSubtask(subtask2);
        savedSubtask2.setId(savedSubtask1.getId());
        taskManager.updateSubtask(savedSubtask2);

        assertEquals(savedSubtask1, savedSubtask2, "Подзадачи не равны друг другу");
        assertEquals(savedSubtask1.hashCode(), savedSubtask2.hashCode(), "Подзадачи не равны друг другу");
    }
}