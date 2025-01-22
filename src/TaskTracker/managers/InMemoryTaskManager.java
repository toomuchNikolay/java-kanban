package TaskTracker.managers;

import TaskTracker.interfaces.*;
import TaskTracker.storage.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager = Manager.getDefaultHistory();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        idCounter = 0;
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
    public List<Task> getAllTask() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(Integer id) {
        historyManager.add(tasks.get(id));
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
    public List<Epic> getAllEpic() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(Integer id) {
        historyManager.add(epics.get(id));
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
    public List<Subtask> getAllSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(Integer id) {
        historyManager.add(subtasks.get(id));
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
    public List<Subtask> getAllSubtaskByEpic(int id) {
        List<Subtask> result = new ArrayList<>();
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
            List<Subtask> subtasksForCheckStatus = getAllSubtaskByEpic(id);
            epic.updateStatus(subtasksForCheckStatus);
        }
    }

    // Получение списка истории просмотра
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}