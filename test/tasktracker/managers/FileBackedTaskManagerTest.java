package tasktracker.managers;

import org.junit.jupiter.api.Test;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends GeneralTaskManagerTest<FileBackedTaskManager> {
    protected File file;

    @Override
    protected FileBackedTaskManager getTaskManager() {
        try {
            file = File.createTempFile("temp", ".scv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (FileBackedTaskManager) Manager.getFileBacked(file.toPath());
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

            assertTrue(result.contains("id,type,title,status,description,epic"),
                    "Сохраненный файл не содержит заголовок");
            assertTrue(result.contains("1,TASK,test task title,NEW,test task description"),
                    "В файл не сохранилась задача");
            assertTrue(result.contains("2,EPIC,test epic title,NEW,test epic description"),
                    "В файл не сохранился эпик");
            assertTrue(result.contains("4,SUBTASK,test subtask2 title,NEW,test subtask2 description,2"),
                    "В файл не сохранилась подзадача");
            assertEquals(5, result.size(), "В файле содержится не 4 задачи + заголовок");
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }
    }

    @Test
    void shouldConvertStringToCorrectTask() {
        Task savedTask1 = FileBackedTaskManager.fromString("1,TASK,test task title,NEW,test task description");
        Task savedTask2 = FileBackedTaskManager.fromString("2,EPIC,test epic title,NEW,test epic description");
        Task savedTask3 = FileBackedTaskManager.fromString("3,SUBTASK,test subtask1 title,NEW,test subtask1 description,2");

        assertEquals(TypeTask.TASK, savedTask1.getType(), "При преобразовании строки вернулся не объект Task");
        assertEquals(TypeTask.EPIC, savedTask2.getType(), "При преобразовании строки вернулся не объект Epic");
        assertEquals(TypeTask.SUBTASK, savedTask3.getType(), "При преобразовании строки вернулся не объект Subtask");
    }

    @Test
    void shouldLoadTasksFromFile() throws IOException {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> taskList1 = manager.getAllTask();
        List<Task> taskList2 = taskManager.getAllTask();
        List<Epic> epicList1 = manager.getAllEpic();
        List<Epic> epicList2 = taskManager.getAllEpic();
        List<Subtask> subtasksList1 = manager.getAllSubtask();
        List<Subtask> subtasksList2 = taskManager.getAllSubtask();

        assertEquals(taskList1, taskList2, "Задачи из файла не восстановились");
        assertEquals(epicList1, epicList2, "Эпики из файла не восстановились");
        assertEquals(subtasksList1, subtasksList2, "Подзадачи из файла не восстановились");
    }

    @Test
    void shouldRestoreIdCounter() {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);

        Task task = new Task("test newtask title", "test newtask description");
        taskManager.addTask(task);

        assertEquals(5, task.getId(), "Не восстановился счетчик id");
    }
}