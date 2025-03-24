package tasktracker.storage;

import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private final TaskManager taskManager = Manager.getDefault();
    private Epic savedEpic;
    private Subtask savedSubtask1;

    @BeforeEach
    void beforeEach() {
        Epic epic = new Epic("test epic title", "test epic description");
        savedEpic = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("test subtask1 title", "test subtask1 description",
                30, "01.01.2025 00:00", savedEpic.getId());
        savedSubtask1 = taskManager.addSubtask(subtask1);
    }

    @Test
    void shouldNotEqualsNull() {
        assertNotEquals(savedSubtask1, null, "Подзадача и null равны");
    }

    @Test
    void shouldNotEqualsAnotherClass() {
        savedEpic.setId(savedSubtask1.getId());

        assertNotEquals(savedSubtask1, savedEpic, "Задачи разного типа равны");
    }

    @Test
    void shouldSubtasksEqualsWithSameId() {
        Subtask subtask2 = new Subtask("test subtask2 title", "test subtask2 description",
                30, "01.01.2025 01:00", savedEpic.getId());
        Subtask savedSubtask2 = taskManager.addSubtask(subtask2);
        savedSubtask2.setId(savedSubtask1.getId());

        assertEquals(savedSubtask1, savedSubtask2, "Подзадачи не равны друг другу");
    }
}