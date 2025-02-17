package tasktracker.interfaces;

import tasktracker.storage.*;

import java.util.List;

public interface TaskManager {
    Task addTask(Task task);

    void updateTask(Task task);

    List<Task> getAllTask();

    Task getTask(Integer id);

    void removeAllTask();

    void removeTask(Integer id);

    Epic addEpic(Epic epic);

    void updateEpic(Epic epic);

    List<Epic> getAllEpic();

    Epic getEpic(Integer id);

    void removeAllEpic();

    void removeEpic(Integer id);

    Subtask addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    List<Subtask> getAllSubtask();

    Subtask getSubtask(Integer id);

    void removeAllSubtask();

    void removeSubtask(Integer id);

    List<Subtask> getAllSubtaskByEpic(int id);

    void checkEpicStatus(int id);

    List<Task> getHistory();
}