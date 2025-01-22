package TaskTracker.interfaces;

import TaskTracker.storage.*;

import java.util.List;

public interface TaskManager {
    Task addTask(Task task);
    void updateTask(Task task);
    List<Task> getAllTask();
    Task getTask(Integer id);
    void removeAllTask();
    void deleteTask(Integer id);

    Epic addEpic(Epic newEpic);
    void updateEpic(Epic updateEpic);
    List<Epic> getAllEpic();
    Epic getEpic(Integer id);
    void removeAllEpic();
    void deleteEpic(Integer id);

    Subtask addSubtask(Subtask newSubtask);
    void updateSubtask(Subtask updateSubtask);
    List<Subtask> getAllSubtask();
    Subtask getSubtask(Integer id);
    void removeAllSubtask();
    void deleteSubtask(Integer id);

    List<Subtask> getAllSubtaskByEpic(int id);
    void checkEpicStatus(int id);
    List<Task> getHistory();
}