package tasktracker.managers;

import tasktracker.interfaces.*;
import tasktracker.storage.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int idCounter;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        idCounter = 0;
        historyManager = Manager.getDefaultHistory();
    }

    private int getIdCounter() {
        return ++idCounter;
    }

    // Методы для Task
    @Override
    public Task addTask(Task task) {
        if (Objects.nonNull(task)) {
            task.setId(getIdCounter());
            tasks.put(task.getId(), task);
        }
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId()))
            tasks.put(task.getId(), task);
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
        for (Task task : tasks.values())
            historyManager.remove(task.getId());
        tasks.clear();
    }

    @Override
    public void removeTask(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // Методы для Epic
    @Override
    public Epic addEpic(Epic epic) {
        if (Objects.nonNull(epic)) {
            epic.setId(getIdCounter());
            epics.put(epic.getId(), epic);
        }
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic newEpic = epics.get(epic.getId());
            newEpic.setTitle(epic.getTitle());
            newEpic.setDescription(epic.getDescription());
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
        for (Epic epic : epics.values())
            historyManager.remove(epic.getId());
        for (Subtask subtask : subtasks.values())
            historyManager.remove(subtask.getId());
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeEpic(Integer id) {
        for (Integer subtaskId : epics.get(id).getSubtasksIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    // Методы для Subtask
    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (Objects.nonNull(subtask) && epics.containsKey(subtask.getEpicId())) {
            subtask.setId(getIdCounter());
            subtasks.put(subtask.getId(), subtask);
            getEpic(subtask.getEpicId()).getSubtasksIds().add(subtask.getId());
            checkEpicStatus(subtask.getEpicId());
        }
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            checkEpicStatus(subtask.getEpicId());
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
        for (Subtask subtask : subtasks.values())
            historyManager.remove(subtask.getId());
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            checkEpicStatus(epic.getId());
        }
    }

    @Override
    public void removeSubtask(Integer id) {
        if (subtasks.containsKey(id)) {
            Subtask deleteSubtask = subtasks.remove(id);
            epics.get(deleteSubtask.getEpicId()).getSubtasksIds().remove(id);
            checkEpicStatus(deleteSubtask.getEpicId());
            historyManager.remove(id);
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
        epics.get(id).setStatus(getAllSubtaskByEpic(id));
    }

    // Получение списка истории просмотра
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}