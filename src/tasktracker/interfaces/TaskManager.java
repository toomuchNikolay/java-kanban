package tasktracker.interfaces;

import tasktracker.storage.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TaskManager {
    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Optional<Task> getTask(Integer id);

    Epic getEpic(Integer id);

    Optional<Subtask> getSubtask(Integer id);

    void removeTask(Integer id);

    void removeEpic(Integer id);

    void removeSubtask(Integer id);

    List<Task> getTaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskList();

    void clearTaskList();

    void clearEpicList();

    void clearSubtaskList();

    List<Subtask> getAllSubtaskByEpic(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    Set<LocalDateTime> checkAvailableTime(LocalDateTime start, LocalDateTime end);
}