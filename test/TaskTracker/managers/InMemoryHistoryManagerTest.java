package TaskTracker.managers;

import TaskTracker.interfaces.*;
import TaskTracker.storage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final HistoryManager historyManager = Manager.getDefaultHistory();
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("test task1 title", "test task1 description");
        task2 = new Task("test task2 title", "test task2 description");
        task3 = new Task("test task3 title", "test task3 description");
        historyManager.add(task1);
    }

    @Test
    void shouldNotAddNull() {
        historyManager.add(null);

        assertEquals(1, historyManager.getHistory().size(), "В историю просмотров добавился Null");
    }

    @Test
    void shouldAddTaskToEnd() {
        historyManager.add(task2);

        assertEquals(task2, historyManager.getHistory().getLast(),
                "Просмотренная задача добавляется не в конец списка истории");
    }

    @Test
    void shouldNotAddDuplicateTask() {
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size(),
                "В списке истории дублируются просмотры задач");
    }

    @Test
    void shouldAddNewVersionTaskToEnd() {
        task1.setTitle("test task1 new title");

        assertEquals("test task1 title", historyManager.getHistory().getLast().getTitle(),
                "В списке истории изменилось название задачи до просмотра");

        historyManager.add(task1);

        assertEquals("test task1 new title", historyManager.getHistory().getLast().getTitle(),
                "При наличии дубля задачи в списке истории добавилась не новая версия");
    }

    @Test
    void shouldRemoveTask() {
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        assertFalse(historyManager.getHistory().contains(task2), "Задача не удалилась из списка истории");
    }
}