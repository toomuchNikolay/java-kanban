package TaskTracker.managers;

import TaskTracker.interfaces.*;
import TaskTracker.storage.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        idCounter = 0;
        this.historyManager = historyManager;
    }

    private int getIdCounter() {
        return ++idCounter;
    }

    // Методы для задач(TaskTracker.storage.Task)
    @Override
    public Task addTask(Task newTask) {
        newTask.setId(getIdCounter());
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }

    @Override
    public void updateTask(Task updateTask) {
        if (tasks.containsKey(updateTask.getId()))
            tasks.put(updateTask.getId(), updateTask);
    }

    @Override
    public ArrayList<Task> getAllTask() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(Integer id) {
        Task copy = new Task(tasks.get(id).getTitle(), tasks.get(id).getDescription());
        copy.setId(tasks.get(id).getId());
        copy.setStatus(tasks.get(id).getStatus());
        historyManager.add(copy);
        return tasks.get(id);
    }

    @Override
    public void removeAllTask() {
        tasks.clear();
    }

    @Override
    public void deleteTask(Integer id) {
            tasks.remove(id);
    }

    // Методы для эпик(TaskTracker.storage.Epic)
    @Override
    public Epic addEpic(Epic newEpic) {
        newEpic.setId(getIdCounter());
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    @Override
    public void updateEpic(Epic updateEpic) {
        if (epics.containsKey(updateEpic.getId())) {
            Epic epic = epics.get(updateEpic.getId());
            epic.setTitle(updateEpic.getTitle());
            epic.setDescription(updateEpic.getDescription());
        }
    }

    @Override
    public ArrayList<Epic> getAllEpic() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic copy = new Epic(epics.get(id).getTitle(), epics.get(id).getDescription());
        copy.setId(epics.get(id).getId());
        copy.setStatus(epics.get(id).getStatus());
        for (Subtask sub : subtasks.values()) {
            if (sub.getEpicId() == id)
                copy.addSubtaskToEpic(sub);
        }
        historyManager.add(copy);
        return epics.get(id);
    }

    @Override
    public void removeAllEpic() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpic(Integer id) {
        for (Integer subtaskId : epics.get(id).getSubtasksIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    // Методы для подзадач(TaskTracker.storage.Subtask)
    @Override
    public Subtask addSubtask(Subtask newSubtask) {
        if (epics.containsKey(newSubtask.getEpicId()) && (newSubtask.getId() != newSubtask.getEpicId())) {
            newSubtask.setId(getIdCounter());
            subtasks.put(newSubtask.getId(), newSubtask);
            getEpic(newSubtask.getEpicId()).addSubtaskToEpic(newSubtask);
            checkEpicStatus(newSubtask.getEpicId());
        }
        return newSubtask;
    }

    @Override
    public void updateSubtask(Subtask updateSubtask) {
        if (subtasks.containsKey(updateSubtask.getId())) {
            subtasks.put(updateSubtask.getId(), updateSubtask);
            checkEpicStatus(updateSubtask.getEpicId());
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(Integer id) {
        Subtask copy = new Subtask(subtasks.get(id).getTitle(), subtasks.get(id).getDescription(), subtasks.get(id).getEpicId());
        copy.setId(subtasks.get(id).getId());
        copy.setStatus(subtasks.get(id).getStatus());
        historyManager.add(copy);
        return subtasks.get(id);
    }

    @Override
    public void removeAllSubtask() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            checkEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteSubtask(Integer id) {
        if (subtasks.containsKey(id)) {
            Subtask deleteSubtask = subtasks.remove(id);
            epics.get(deleteSubtask.getEpicId()).getSubtasksIds().remove(id);
            checkEpicStatus(deleteSubtask.getEpicId());
        }
    }

    // Получение списка всех подзадач определённого эпика
    @Override
    public ArrayList<Subtask> getAllSubtaskByEpic(int id) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == id)
                result.add(subtask);
        }
        return result;
    }

    // Получение статуса эпика
    @Override
    public void checkEpicStatus(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            ArrayList<Subtask> subtasksForCheckStatus = getAllSubtaskByEpic(id);
            epic.updateStatus(subtasksForCheckStatus);
        }
    }
}