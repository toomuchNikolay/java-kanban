package TaskTracker.managers;

import TaskTracker.interfaces.*;
import TaskTracker.storage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final HistoryManager historyManager = Manager.getDefaultHistory();
    private Task firstTask;

    @BeforeEach
    void beforeEach() {
        firstTask = new Task("test first task title", "test first task description");
        historyManager.add(firstTask);
        historyManager.add(new Task("test task1 title", "test task1 description"));
        historyManager.add(new Epic("test epic title", "test epic description"));
        historyManager.add(new Subtask("test subtask title", "test subtask description", 3));
        historyManager.add(new Task("test task2 title", "test task2 description"));
        historyManager.add(new Task("test task3 title", "test task3 description"));
        historyManager.add(new Task("test task4 title", "test task4 description"));
        historyManager.add(new Task("test task5 title", "test task5 description"));
        historyManager.add(new Task("test task6 title", "test task6 description"));
    }

    @Test
    void shouldHistoryManagerSavePreviousVersionTask() {
        firstTask.setTitle("test first task title new");
        historyManager.add(firstTask);

        assertEquals("test first task title", historyManager.getHistory().getFirst().getTitle(),
                "Название первой просмотренной задачи не совпадает");
        assertEquals("test first task title new", historyManager.getHistory().getLast().getTitle(),
                "Название последней просмотренной задачи не совпадает");
    }

    @Test
    void shouldReturnTrueSizeHistoryList() {
        assertEquals(9, historyManager.getHistory().size(),
                "Не совпадает количество записей в истории просмотров");
    }

    @Test
    void shouldNotBeMoreMaxSizeHistoryList() {
        Task lastTask = new Task("test last task title", "test last task description");
        historyManager.add(lastTask);
        Task overMaxSizeTask = new Task("test over task title", "test over task description");
        historyManager.add(overMaxSizeTask);

        assertTrue(historyManager.getHistory().size() <= 10,
                "Превышен максимально допустимый размер истории просмотров");
    }

    @Test
    void shouldDeleteFirstTaskAndAddLast() {
        Task lastTask = new Task("test last task title", "test last task description");
        historyManager.add(lastTask);
        Task overMaxSizeTask = new Task("test over task title", "test over task description");
        historyManager.add(overMaxSizeTask);

        assertEquals(overMaxSizeTask, historyManager.getHistory().getLast(),
                "В историю просмотров не добавилась последняя просмотренная задача при достижении лимита");
        assertNotEquals("test first task title", historyManager.getHistory().getFirst().getTitle(),
                "В истории просмотров при достижении лимита удалилась не первая просмотренная задача");
    }
}