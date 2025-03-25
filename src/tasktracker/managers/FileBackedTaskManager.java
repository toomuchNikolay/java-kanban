package tasktracker.managers;

import tasktracker.exceptions.*;
import tasktracker.storage.*;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected Path path;

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
    }

    protected void save() {
        try (Writer writer = new FileWriter(path.toFile())) {
            String header = "id,type,title,status,description,duration,start,epic";
            writer.write(header + "\n");
            for (Task task : getTaskList()) {
                writer.write(task.toString() + "\n");
            }
            for (Task task : getEpicList()) {
                writer.write(task.toString() + "\n");
            }
            for (Task task : getSubtaskList()) {
                writer.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения задач в файл");
        }
    }

    protected static Task fromString(String value) {
        String[] split = value.split(",");

        int id = Integer.parseInt(split[0]);
        TypeTask type;
        try {
            type = TypeTask.valueOf(split[1]);
        } catch (IllegalArgumentException e) {
            throw new ManagerLoadException("Ошибка создания задачи - некорректный тип задачи");
        }
        String title = split[2];
        String description = split[4];
        StatusTask status = StatusTask.valueOf(split[3]);
        Duration duration = null;
        LocalDateTime startTime = null;
        Integer epicId = null;

        if (split.length > 6) {
            duration = Duration.ofMinutes(Long.parseLong(split[5]));
            startTime = LocalDateTime.parse(split[6], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        if (type == TypeTask.SUBTASK) {
            if (split.length > 6)
                epicId = Integer.parseInt(split[7]);
            else
                epicId = Integer.parseInt(split[5]);
        }

        return switch (type) {
            case TypeTask.TASK -> new Task(id, title, description, status, duration, startTime);
            case TypeTask.EPIC -> new Epic(id, title, description, status, duration, startTime);
            case TypeTask.SUBTASK -> new Subtask(id, title, description, status, duration, startTime, epicId);
        };
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while (br.ready()) {
                String line = br.readLine();
                Task createTask = fromString(line);
                if (createTask.getId() > manager.idCounter)
                    manager.idCounter = createTask.getId();
                if (createTask instanceof Epic) {
                    manager.epics.put(createTask.getId(), (Epic) createTask);
                } else if (createTask instanceof Subtask) {
                    manager.subtasks.put(createTask.getId(), (Subtask) createTask);
                    manager.getEpic(((Subtask) createTask).getEpicId()).getSubtasksIds().add(createTask.getId());
                    if (createTask.getStartTime() != null && createTask.getDuration() != null) {
                        manager.timeSlotBooking(createTask);
                        manager.addToPrioritizedTasks(createTask);
                        manager.timeCalculationEpic(((Subtask) createTask).getEpicId());
                    }
                    manager.checkEpicStatus(((Subtask) createTask).getEpicId());
                } else {
                    manager.tasks.put(createTask.getId(), createTask);
                    if (createTask.getStartTime() != null && createTask.getDuration() != null) {
                        manager.timeSlotBooking(createTask);
                        manager.addToPrioritizedTasks(createTask);
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
    public Epic addEpic(Epic epic) {
        Epic savedEpic = super.addEpic(epic);
        save();
        return savedEpic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask savedSubtask = super.addSubtask(subtask);
        save();
        return savedSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        Task savedTask = super.updateTask(task);
        save();
        return savedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic savedEpic = super.updateEpic(epic);
        save();
        return savedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask savedSubtask = super.updateSubtask(subtask);
        save();
        return savedSubtask;
    }

    @Override
    public Task removeTask(Integer id) {
        Task savedTask = super.removeTask(id);
        save();
        return savedTask;
    }

    @Override
    public Epic removeEpic(Integer id) {
        Epic savedEpic = super.removeEpic(id);
        save();
        return savedEpic;
    }

    @Override
    public Subtask removeSubtask(Integer id) {
        Subtask savedSubtask = super.removeSubtask(id);
        save();
        return savedSubtask;
    }

    @Override
    public void clearTaskList() {
        super.clearTaskList();
        save();
    }

    @Override
    public void clearEpicList() {
        super.clearEpicList();
        save();
    }

    @Override
    public void clearSubtaskList() {
        super.clearSubtaskList();
        save();
    }
}