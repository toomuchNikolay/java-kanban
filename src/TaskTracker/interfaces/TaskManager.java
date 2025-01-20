package TaskTracker.interfaces;

import TaskTracker.storage.*;

import java.util.ArrayList;

public interface TaskManager {
    Task addTask(Task task);
    void updateTask(Task task);
    ArrayList<Task> getAllTask();
    Task getTask(Integer id);
    void removeAllTask();
    void deleteTask(Integer id);

    Epic addEpic(Epic newEpic);
    void updateEpic(Epic updateEpic);
    ArrayList<Epic> getAllEpic();
    Epic getEpic(Integer id);
    void removeAllEpic();
    void deleteEpic(Integer id);

    Subtask addSubtask(Subtask newSubtask);
    void updateSubtask(Subtask updateSubtask);
    ArrayList<Subtask> getAllSubtask();
    Subtask getSubtask(Integer id);
    void removeAllSubtask();
    void deleteSubtask(Integer id);

    ArrayList<Subtask> getAllSubtaskByEpic(int id);
    void checkEpicStatus(int id);
}