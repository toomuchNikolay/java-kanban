package tasktracker.managers;

import tasktracker.interfaces.*;
import tasktracker.storage.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected HistoryManager historyManager;
    protected int idCounter;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected Set<Task> prioritizedTasks;
    protected Map<LocalDateTime, Boolean> availableTime;
    protected static final LocalDateTime START_PERIOD = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final LocalDateTime END_PERIOD = LocalDateTime.of(2025, 12, 31, 23, 45);

    public InMemoryTaskManager() {
        historyManager = Manager.getDefaultHistory();
        idCounter = 0;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        this.availableTime = createTableTime();
    }

    private int getIdCounter() {
        return ++idCounter;
    }

    protected Map<LocalDateTime, Boolean> createTableTime() {
        availableTime = new HashMap<>();
        LocalDateTime current = START_PERIOD;

        while (current.isBefore(END_PERIOD)) {
            availableTime.put(current, true);
            current = current.plusMinutes(15);
        }
        return availableTime;
    }

    protected boolean isTimeAvailable(Task task) {
        LocalDateTime head = task.getStartTime();
        LocalDateTime tail = head.plus(task.getDuration());

        while (head.isBefore(tail)) {
            if (!availableTime.get(head))
                return false;
            head = head.plusMinutes(15);
        }
        return true;
    }

    protected void timeSlotBooking(Task task) {
        LocalDateTime head = task.getStartTime();
        LocalDateTime tail = head.plus(task.getDuration());

        while (head.isBefore(tail)) {
            availableTime.put(head, false);
            head = head.plusMinutes(15);
        }
    }

    protected void timeSlotFree(Task task) {
        LocalDateTime head = task.getStartTime();
        LocalDateTime tail = head.plus(task.getDuration());

        while (head.isBefore(tail)) {
            availableTime.put(head, true);
            head = head.plusMinutes(15);
        }
    }

    @Override
    public Set<LocalDateTime> checkAvailableTime(LocalDateTime start, LocalDateTime end) {
        return availableTime.entrySet().stream()
                .filter(time -> time.getKey().isAfter(start) && time.getKey().isBefore(end))
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Task addTask(Task task) {
        if (task == null)
            return task;
        if (task.getStartTime() != null && task.getDuration() != null) {
            if (isTimeAvailable(task)) {
                timeSlotBooking(task);
                addToPrioritizedTasks(task);
            } else {
                return task;
            }
        }
        task.setId(getIdCounter());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (Objects.nonNull(epic)) {
            epic.setId(getIdCounter());
            epics.put(epic.getId(), epic);
        }
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId()))
            return subtask;
        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (isTimeAvailable(subtask)) {
                timeSlotBooking(subtask);
                addToPrioritizedTasks(subtask);
            } else {
                return subtask;
            }
        }
        subtask.setId(getIdCounter());
        subtasks.put(subtask.getId(), subtask);
        getEpic(subtask.getEpicId()).getSubtasksIds().add(subtask.getId());
        timeCalculationEpic(subtask.getEpicId());
        checkEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        if (task == null)
            return task;
        Task old = tasks.get(task.getId());
        if (old == null)
            return task;
        if (old.getStartTime() != null && old.getDuration() != null) {
            prioritizedTasks.remove(old);
            timeSlotFree(old);
        }
        if (task.getStartTime() != null && task.getDuration() != null) {
            if (!isTimeAvailable(task)) {
                addToPrioritizedTasks(old);
                timeSlotBooking(old);
                return old;
            }
            addToPrioritizedTasks(task);
            timeSlotBooking(task);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic old = epics.get(epic.getId());
        if (old == null)
            return epic;
        old.setTitle(epic.getTitle());
        old.setDescription(epic.getDescription());
        return old;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null)
            return subtask;
        if (!epics.containsKey(subtask.getEpicId()))
            return subtask;
        Subtask old = subtasks.get(subtask.getId());
        if (old == null)
            return subtask;
        if (old.getStartTime() != null && old.getDuration() != null) {
            prioritizedTasks.remove(old);
            timeSlotFree(old);
        }
        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (!isTimeAvailable(subtask)) {
                addToPrioritizedTasks(old);
                timeSlotBooking(old);
                return old;
            }
            addToPrioritizedTasks(subtask);
            timeSlotBooking(subtask);
        }
        getEpic(old.getEpicId()).getSubtasksIds().clear();
        getEpic(subtask.getEpicId()).getSubtasksIds().clear();
        subtasks.put(subtask.getId(), subtask);
        getEpic(old.getEpicId()).getSubtasksIds().addAll(subtasks.values().stream()
                .filter(s -> s.getEpicId() == old.getEpicId())
                .map(Task::getId).toList());
        getEpic(subtask.getEpicId()).getSubtasksIds().addAll(subtasks.values().stream()
                .filter(s -> s.getEpicId() == subtask.getEpicId())
                .map(Task::getId).toList());
        timeCalculationEpic(old.getEpicId());
        checkEpicStatus(old.getEpicId());
        timeCalculationEpic(subtask.getEpicId());
        checkEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public Optional<Task> getTask(Integer id) {
        Optional<Task> result = Optional.ofNullable(tasks.get(id));
        result.ifPresent(historyManager::add);
        return result;
    }

    @Override
    public Epic getEpic(Integer id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Optional<Subtask> getSubtask(Integer id) {
        Optional<Subtask> result = Optional.ofNullable(subtasks.get(id));
        result.ifPresent(historyManager::add);
        return result;
    }

    @Override
    public void removeTask(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(Integer id) {
        epics.get(id).getSubtasksIds().forEach(i -> {
            subtasks.remove(i);
            historyManager.remove(i);
        });
        epics.remove(id);
        historyManager.remove(id);
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

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearTaskList() {
        tasks.values().stream()
                .map(Task::getId)
                .forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void clearEpicList() {
        Stream.concat(epics.values().stream(), subtasks.values().stream())
                .map(Task::getId)
                .forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtaskList() {
        subtasks.values().stream()
                .map(Task::getId)
                .forEach(historyManager::remove);
        subtasks.clear();
        epics.values().stream()
                .map(Epic::getId)
                .forEach(i -> {
                    getEpic(i).getSubtasksIds().clear();
                    checkEpicStatus(i);
                });
    }

    @Override
    public List<Subtask> getAllSubtaskByEpic(int id) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == id)
                .collect(Collectors.toList());
    }

    protected void checkEpicStatus(int id) {
        epics.get(id).setStatus(getAllSubtaskByEpic(id));
    }

    protected void timeCalculationEpic(int id) {
        epics.get(id).setDuration(getAllSubtaskByEpic(id));
        epics.get(id).setStartTime(getAllSubtaskByEpic(id));
        epics.get(id).setEndTime(getAllSubtaskByEpic(id));
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null)
            prioritizedTasks.add(task);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}