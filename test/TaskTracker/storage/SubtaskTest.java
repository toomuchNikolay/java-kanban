package TaskTracker.storage;

import TaskTracker.interfaces.TaskManager;
import TaskTracker.managers.Manager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    TaskManager taskManager = Manager.getDefault();

    @Test
    void shouldBePositiveWhenIdSubtasksEquals() {
        Epic epic = new Epic("test title", "test desc");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("test1 title", "test1 desc", epic.getId());
        Subtask savedSubtask1 = taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("test2 title", "test2 desc", epic.getId());
        Subtask savedSubtask2 = taskManager.addSubtask(subtask2);
        savedSubtask2.setId(savedSubtask1.getId());

        assertEquals(savedSubtask1, savedSubtask2, "Задачи не равны друг другу");
        assertEquals(savedSubtask1.hashCode(), savedSubtask2.hashCode(), "Задачи не равны друг другу");
    }

    @Test
    void shouldReturnNullSubtasksWhenIdSubtaskEqualsEpicId() {
        Epic epic = new Epic("test title", "test desc");
        Epic savedEpic = taskManager.addEpic(epic);
        savedEpic.setId(2);
        Subtask subtask = new Subtask("test1 title", "test1 desc", savedEpic.getId());
        taskManager.addSubtask(subtask);
        final ArrayList<Subtask> subtasks = taskManager.getAllSubtask();

        assertTrue(subtasks.isEmpty(), "Список подзадач не пустой");
    }
}