package tasktracker.managers;

import tasktracker.exceptions.*;
import tasktracker.storage.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected Path path;
    final String header = "id,type,title,status,description,epic";

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("temp", ".scv");
        FileBackedTaskManager manager = Manager.getDefaultBacked(file.toPath());

        Task task = new Task("task title", "task desc");
        manager.addTask(task);
        Epic firstEpic = new Epic("first epic title", "first epic desc");
        manager.addEpic(firstEpic);
        Epic secondEpic = new Epic("second epic title", "second epic desc");
        manager.addEpic(secondEpic);
        Subtask firstSubtask = new Subtask("first subtask title", "first subtask desc", firstEpic.getId());
        manager.addSubtask(firstSubtask);
        Subtask secondSubtask = new Subtask("second subtask title", "second subtask desc", secondEpic.getId());
        manager.addSubtask(secondSubtask);

        FileBackedTaskManager backedTaskManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> taskList1 = manager.getAllTask();
        List<Task> taskList2 = backedTaskManager.getAllTask();
        List<Epic> epicList1 = manager.getAllEpic();
        List<Epic> epicList2 = backedTaskManager.getAllEpic();
        List<Subtask> subtasksList1 = manager.getAllSubtask();
        List<Subtask> subtasksList2 = backedTaskManager.getAllSubtask();

        System.out.println("Списки задач из менеджеров идентичны: " + compare(taskList1, taskList2));
        System.out.println("Списки эпиков из менеджеров идентичны: " + compare(epicList1, epicList2));
        System.out.println("Списки подзадач из менеджеров идентичны: " + compare(subtasksList1, subtasksList2));
    }

    private static boolean compare(List<? extends Task> list1, List<? extends Task> list2) {
        return list1.equals(list2);
    }

    public void save() {
        try (Writer writer = new FileWriter(path.toFile())) {
            writer.write(header + "\n");
            for (Task task : getAllTask()) {
                writer.write(task.toString() + "\n");
            }
            for (Task task : getAllEpic()) {
                writer.write(task.toString() + "\n");
            }
            for (Task task : getAllSubtask()) {
                writer.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения задач в файл");
        }
    }

    protected static Task fromString(String value) {
        String[] split = value.split(",");
        switch (split[1]) {
            case "TASK": {
                return new Task(Integer.parseInt(split[0]), split[2], split[4], StatusTask.valueOf(split[3]));
            }
            case "EPIC": {
                return new Epic(Integer.parseInt(split[0]), split[2], split[4], StatusTask.valueOf(split[3]));
            }
            case "SUBTASK": {
                return new Subtask(Integer.parseInt(split[0]), split[2], split[4], StatusTask.valueOf(split[3]), Integer.parseInt(split[5]));
            }
            default:
                return null;
        }
    }

    protected static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while (br.ready()) {
                String line = br.readLine();
                Task createTask = fromString(line);
                if (createTask != null) {
                    if (createTask.getId() > manager.idCounter)
                        manager.idCounter = createTask.getId();
                    if (createTask instanceof Epic) {
                        manager.epics.put(createTask.getId(), (Epic) createTask);
                    } else if (createTask instanceof Subtask) {
                        manager.subtasks.put(createTask.getId(), (Subtask) createTask);
                        manager.getEpic(((Subtask) createTask).getEpicId()).getSubtasksIds().add(createTask.getId());
                        manager.checkEpicStatus(((Subtask) createTask).getEpicId());
                    } else {
                        manager.tasks.put(createTask.getId(), createTask);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка загрузки задач из файла");
        }
        return manager;
    }

    @Override
    public Task addTask(Task task) {
        Task savedTask = super.addTask(task);
        save();
        return savedTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeAllTask() {
        super.removeAllTask();
        save();
    }

    @Override
    public void removeTask(Integer id) {
        super.removeTask(id);
        save();
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic savedEpic = super.addEpic(epic);
        save();
        return savedEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeAllEpic() {
        super.removeAllEpic();
        save();
    }

    @Override
    public void removeEpic(Integer id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask savedSubtask = super.addSubtask(subtask);
        save();
        return savedSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeAllSubtask() {
        super.removeAllSubtask();
        save();
    }

    @Override
    public void removeSubtask(Integer id) {
        super.removeSubtask(id);
        save();
    }
}