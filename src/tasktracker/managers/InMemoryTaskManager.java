package tasktracker.managers;

import tasktracker.exceptions.NotFoundException;
import tasktracker.exceptions.TaskValidationException;
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

    private Map<LocalDateTime, Boolean> createTableTime() {
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
                .filter(time -> !time.getKey().isBefore(start) && time.getKey().isBefore(end))
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Task addTask(Task task) {
        if (task == null) {
            return task;
        }
        if (task.getStartTime() != null && task.getDuration() != null) {
            if (isTimeAvailable(task)) {
                timeSlotBooking(task);
                addToPrioritizedTasks(task);
            } else {
                throw new TaskValidationException();
            }
        }
        task.setId(getIdCounter());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return epic;
        }
        epic.setId(getIdCounter());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null)
            return subtask;
        if (!epics.containsKey(subtask.getEpicId()))
            throw new NotFoundException("В списке отсутствует эпик с указанным id");
        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (isTimeAvailable(subtask)) {
                timeSlotBooking(subtask);
                addToPrioritizedTasks(subtask);
            } else {
                throw new TaskValidationException();
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
            throw new NotFoundException("В списке отсутствует задача с переданным id");
        if (old.getStartTime() != null && old.getDuration() != null) {
            prioritizedTasks.remove(old);
            timeSlotFree(old);
        }
        if (task.getStartTime() != null && task.getDuration() != null) {
            if (!isTimeAvailable(task)) {
                addToPrioritizedTasks(old);
                timeSlotBooking(old);
                throw new TaskValidationException();
            }
            addToPrioritizedTasks(task);
            timeSlotBooking(task);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null)
            return epic;
        Epic old = epics.get(epic.getId());
        if (old == null)
            throw new NotFoundException("В списке отсутствует эпик с переданным id");
        old.setTitle(epic.getTitle());
        old.setDescription(epic.getDescription());
        return old;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId()))
            return subtask;
        Subtask old = subtasks.get(subtask.getId());
        if (old == null)
            throw new NotFoundException("В списке отсутствует подзадача с переданным id");
        if (old.getStartTime() != null && old.getDuration() != null) {
            prioritizedTasks.remove(old);
            timeSlotFree(old);
        }
        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (!isTimeAvailable(subtask)) {
                addToPrioritizedTasks(old);
                timeSlotBooking(old);
                throw new TaskValidationException();
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
    public Task getTask(Integer id) {
        Task task = tasks.get(id);
        if (task == null)
            throw new NotFoundException("Задача не найдена");
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epics.get(id);
        if (epic == null)
            throw new NotFoundException("Эпик не найден");
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(Integer id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null)
            throw new NotFoundException("Подзадача не найдена");
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Task removeTask(Integer id) {
        Task deleteTask = tasks.remove(id);
        if (deleteTask == null) {
            throw new NotFoundException("В списке отсутствует задача с переданным id");
        }
        prioritizedTasks.remove(deleteTask);
        timeSlotFree(deleteTask);
        historyManager.remove(id);
        return deleteTask;
    }

    @Override
    public Epic removeEpic(Integer id) {
        Epic deleteEpic = epics.get(id);
        if (deleteEpic == null) {
            throw new NotFoundException("В списке отсутствует эпик с переданным id");
        }
        deleteEpic.getSubtasksIds()
                .forEach(i -> {
                    Subtask deleteSubtask = subtasks.remove(i);
                    prioritizedTasks.remove(deleteSubtask);
                    timeSlotFree(deleteSubtask);
                    historyManager.remove(i);
                });
        epics.remove(id);
        historyManager.remove(id);
        return deleteEpic;
    }

    @Override
    public Subtask removeSubtask(Integer id) {
        Subtask deleteSubtask = subtasks.remove(id);
        if (deleteSubtask == null) {
            throw new NotFoundException("В списке отсутствует подзадача с переданным id");
        }
        epics.get(deleteSubtask.getEpicId()).getSubtasksIds().remove(id);
        checkEpicStatus(deleteSubtask.getEpicId());
        prioritizedTasks.remove(deleteSubtask);
        timeSlotFree(deleteSubtask);
        historyManager.remove(id);
        return deleteSubtask;
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
        tasks.values()
                .forEach(task -> {
                    timeSlotFree(task);
                    historyManager.remove(task.getId());
                });
        prioritizedTasks.removeIf(task -> task.getType().equals(TypeTask.TASK));
        tasks.clear();
    }

    @Override
    public void clearEpicList() {
        Stream.concat(epics.values().stream(), subtasks.values().stream())
                .forEach(task -> {
                    timeSlotFree(task);
                    historyManager.remove(task.getId());
                });
        prioritizedTasks.removeIf(task -> !task.getType().equals(TypeTask.TASK));
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtaskList() {
        subtasks.values()
                .forEach(subtask -> {
                    timeSlotFree(subtask);
                    historyManager.remove(subtask.getId());
                });
        prioritizedTasks.removeIf(task -> task.getType().equals(TypeTask.SUBTASK));
        subtasks.clear();
        epics.values()
                .forEach(epic -> {
                    epic.getSubtasksIds().clear();
                    timeCalculationEpic(epic.getId());
                    checkEpicStatus(epic.getId());
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
        epics.get(id).setEpicDuration(getAllSubtaskByEpic(id));
        epics.get(id).setEpicStartTime(getAllSubtaskByEpic(id));
        epics.get(id).setEpicEndTime(getAllSubtaskByEpic(id));
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