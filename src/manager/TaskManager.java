package manager;

import storageTasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    // Возможность хранить задачи всех типов
    private int idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        idCounter = 0;
    }

    private int getIdCounter() {
        return ++idCounter;
    }

    // Методы для задач(storageTasks.Task)
    public void addTask(Task newTask) {
        newTask.setId(getIdCounter());
        tasks.put(newTask.getId(), newTask);
    }

    public void updateTask(Task updateTask) {
        if (tasks.containsKey(updateTask.getId()))
            tasks.put(updateTask.getId(), updateTask);
    }

    public ArrayList<Task> getAllTask() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public void removeAllTask() {
        tasks.clear();
    }

    public void deleteTask(Integer id) {
            tasks.remove(id);
    }

    // Методы для эпик(storageTasks.Epic)
    public void addEpic(Epic newEpic) {
        newEpic.setId(getIdCounter());
        epics.put(newEpic.getId(), newEpic);
    }

    // Есть сомнения насчет того, что этот вариант "можно лучше" :)
    public void updateEpic(Epic updateEpic) {
        if (epics.containsKey(updateEpic.getId())) {
            Epic tempEpic = epics.get(updateEpic.getId());
            tempEpic.setTitle(updateEpic.getTitle());
            tempEpic.setDescription(updateEpic.getDescription());
            epics.put(updateEpic.getId(), tempEpic);
            checkEpicStatus(updateEpic.getId());
        }
    }

    public ArrayList<Epic> getAllEpic() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    public void removeAllEpic() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteEpic(Integer id) {
        for (Integer subtaskId : epics.get(id).getSubtasksIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    // Методы для подзадач(storageTasks.Subtask)
    public void addSubtask(Subtask newSubtask) {
        if (epics.containsKey(newSubtask.getEpicId())) {
            newSubtask.setId(getIdCounter());
            subtasks.put(newSubtask.getId(), newSubtask);
            getEpicById(newSubtask.getEpicId()).addSubtaskToEpic(newSubtask);
            checkEpicStatus(newSubtask.getEpicId());
        }
    }

    public void updateSubtask(Subtask updateSubtask) {
        if (subtasks.containsKey(updateSubtask.getId())) {
            subtasks.put(updateSubtask.getId(), updateSubtask);
            checkEpicStatus(updateSubtask.getEpicId());
        }
    }

    public ArrayList<Subtask> getAllSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    public Subtask getSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    public void removeAllSubtask() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            checkEpicStatus(epic.getId());
        }
    }

    public void deleteSubtask(Integer id) {
        subtasks.remove(id);
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().remove(id);
            checkEpicStatus(epic.getId());
        }
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Subtask> getAllSubtaskByEpic(int id) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == id)
                result.add(subtask);
        }
        return result;
    }

    // Получение статуса эпика
    public void checkEpicStatus(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            ArrayList<Subtask> subtasksForCheckStatus = getAllSubtaskByEpic(id);
            epic.updateStatus(subtasksForCheckStatus);
        }
    }
}