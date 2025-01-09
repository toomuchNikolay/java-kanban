import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    // Возможность хранить задачи всех типов
    private static int idCounter;
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

    // Методы для задач(Task)
    public void addTask(Task newTask) {
        newTask.setId(getIdCounter());
        tasks.put(newTask.getId(), newTask);
    }

    public void updateTask(Task updateTask) {
        for (Task task : tasks.values()) {
            if (updateTask.getId() == task.getId()) {
                tasks.put(updateTask.getId(), updateTask);
            }
        }
    }

    public ArrayList<Task> getAllTask() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void removeAllTask() {
        tasks.clear();
    }

    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            System.out.println("Задача с id " + id + " удалена.");
        } else
            System.out.println("Задача с указанным id не найдена.");
    }

    // Методы для эпик(Epic)
    public void addEpic(Epic newEpic) {
        newEpic.setId(getIdCounter());
        epics.put(newEpic.getId(), newEpic);
    }

    public void updateEpic(Epic updateEpic) {
        for (Epic epic : epics.values()) {
            if (updateEpic.getId() == epic.getId())
                epics.put(updateEpic.getId(), updateEpic);
        }
    }

    public ArrayList<Epic> getAllEpic() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void removeAllEpic() {
        epics.clear();
    }

    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            epics.remove(id);
            System.out.println("Эпик с id " + id + " удален.");
        } else
            System.out.println("Эпик с указанным id не найден.");
    }

    // Методы для подзадач(Subtask)
    public void addSubtask(Subtask newSubtask) {
        newSubtask.setId(getIdCounter());
        subtasks.put(newSubtask.getId(), newSubtask);
        getEpicById(newSubtask.getEpicId()).addSubtaskToEpic(newSubtask);
    }

    public void updateSubtask(Subtask updateSubtask) {
        for (Subtask subtask : subtasks.values()) {
            if (updateSubtask.getId() == subtask.getId())
                subtasks.put(updateSubtask.getId(), updateSubtask);
        }
    }

    public ArrayList<Subtask> getAllSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void removeAllSubtask() {
        subtasks.clear();
    }

    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            subtasks.remove(id);
            System.out.println("Подзадача с id " + id + " удалена.");
        } else
            System.out.println("Подзадача с указанным id не найдена.");
    }

    // Печать списка задач всех типов
    public void printAllTasks() {
        if (tasks.isEmpty() && epics.isEmpty() && subtasks.isEmpty())
            System.out.println("Список задач пуст");
        else {
            for (Integer key : tasks.keySet()) {
                Task value = tasks.get(key);
                System.out.println(key + " : " + value);
            }

            for (Integer key : epics.keySet()) {
                Task value = epics.get(key);
                System.out.println(key + " : " + value);
            }

            for (Integer key : subtasks.keySet()) {
                Task value = subtasks.get(key);
                System.out.println(key + " : " + value);
            }
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