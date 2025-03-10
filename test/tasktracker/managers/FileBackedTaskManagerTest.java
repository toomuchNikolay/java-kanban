package tasktracker.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import tasktracker.exceptions.ManagerLoadException;
import tasktracker.exceptions.ManagerSaveException;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends GeneralTaskManagerTest<FileBackedTaskManager> {
    protected File file;

    @Override
    protected FileBackedTaskManager getTaskManager() {
        try {
            file = File.createTempFile("temp", ".scv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return FileBackedTaskManager.loadFromFile(file);
    }

    @Test
    void shouldSaveTasksToFile() {
        try {
            List<String> result = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                String line = br.readLine();
                result.add(line);
            }

            assertTrue(result.contains("id,type,title,status,description,duration,start,epic"),
                    "Сохраненный файл не содержит заголовок");
            assertTrue(result.contains("1,TASK,test task title,NEW,test task description,60,01.01.2025 00:00"),
                    "В файл не сохранилась задача");
            assertTrue(result.contains("2,EPIC,test epic title,NEW,test epic description,120,01.01.2025 10:00"),
                    "В файл не сохранился эпик");
            assertTrue(result.contains("4,SUBTASK,test subtask2 title,NEW,test subtask2 description,60,01.01.2025 11:00,2"),
                    "В файл не сохранилась подзадача");
            assertEquals(5, result.size(), "В файле содержится не 4 задачи + заголовок");
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }
    }

    @Test
    void shouldConvertStringToCorrectTask() {
        Task savedTask1 = FileBackedTaskManager.fromString("1,TASK,test task title,NEW,test task description,10,01.01.2025 00:00");
        Task savedTask2 = FileBackedTaskManager.fromString("2,EPIC,test epic title,NEW,test epic description,10,01.01.2025 00:00");
        Task savedTask3 = FileBackedTaskManager.fromString("3,SUBTASK,test subtask1 title,NEW,test subtask1 description,10,01.01.2025 00:00,2");

        assertEquals(TypeTask.TASK, savedTask1.getType(), "При преобразовании строки вернулся не объект Task");
        assertEquals(TypeTask.EPIC, savedTask2.getType(), "При преобразовании строки вернулся не объект Epic");
        assertEquals(TypeTask.SUBTASK, savedTask3.getType(), "При преобразовании строки вернулся не объект Subtask");
    }

    @Test
    void shouldLoadTasksFromFile() throws IOException {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0);

        List<Task> taskList1 = manager.getTaskList();
        List<Task> taskList2 = taskManager.getTaskList();
        List<Epic> epicList1 = manager.getEpicList();
        List<Epic> epicList2 = taskManager.getEpicList();
        List<Subtask> subtasksList1 = manager.getSubtaskList();
        List<Subtask> subtasksList2 = taskManager.getSubtaskList();

        assertEquals(taskList1, taskList2, "Задачи из файла не восстановились");
        assertEquals(epicList1, epicList2, "Эпики из файла не восстановились");
        assertEquals(subtasksList1, subtasksList2, "Подзадачи из файла не восстановились");

        List<Task> prioritizedList1 = manager.getPrioritizedTasks();
        List<Task> prioritizedList2 = taskManager.getPrioritizedTasks();

        assertEquals(prioritizedList1, prioritizedList2, "Список приоритетов не восстановился");

        Set<LocalDateTime> availableTimeSet1 = manager.checkAvailableTime(start, end);
        Set<LocalDateTime> availableTimeSet2 = taskManager.checkAvailableTime(start, end);

        assertEquals(availableTimeSet1, availableTimeSet2, "Таблица доступного времени не восстановилась");
    }

    @Test
    void shouldRestoreIdCounter() {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);

        Task task = new Task("test newtask title", "test newtask description",
                60, "01.01.2025 20:00");
        taskManager.addTask(task);

        assertEquals(5, task.getId(), "Не восстановился счетчик id");
    }

    @Test
    void shouldThrowsManagerSaveException() {
        boolean isReadOnly = file.setReadOnly();

        if (isReadOnly) {
            ManagerSaveException exception = assertThrows(ManagerSaveException.class, () -> {
                manager.save();
            });

            assertEquals("Ошибка сохранения задач в файл", exception.getMessage(),
                    "Исключение при сохранении в файл не выброшено");
        }
    }

    @Test
    void shouldThrowsManagerLoadException() {
        boolean isDelete = file.delete();

        if (isDelete) {
            ManagerLoadException exception = assertThrows(ManagerLoadException.class, () -> {
                FileBackedTaskManager.loadFromFile(file);
            });

            assertEquals("Ошибка загрузки задач из файла", exception.getMessage(),
                    "Исключение при загрузке из файла не выброшено");
        }
    }
}