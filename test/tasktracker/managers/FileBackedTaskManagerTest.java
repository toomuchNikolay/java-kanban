package tasktracker.managers;

import org.junit.jupiter.api.Test;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    @Test
    void shouldSaveTasksToFile() {
        try {
            File tempFile = File.createTempFile("temp", ".scv");
            FileBackedTaskManager backedManager = Manager.getDefaultBacked(tempFile.toPath());

            Task task = new Task("test task title", "test task description");
            backedManager.addTask(task);
            Epic epic = new Epic("test epic title", "test epic description");
            backedManager.addEpic(epic);
            Subtask subtask1 = new Subtask("test subtask1 title", "test subtask1 description", epic.getId());
            backedManager.addSubtask(subtask1);
            Subtask subtask2 = new Subtask("test subtask2 title", "test subtask2 description", epic.getId());
            backedManager.addSubtask(subtask2);

            assertTrue(tempFile.length() != 0, "Файл пустой");

            backedManager.removeSubtask(subtask2.getId());

            BufferedReader br = new BufferedReader(new FileReader(tempFile));
            int lineCount = 0;
            while (br.ready()) {
                br.readLine();
                ++lineCount;
            }

            assertEquals(4, lineCount, "После удаления задачи файл не обновился");

        } catch (IOException e) {
            System.out.println("Ошибка при сохранении файла");
        }
    }

    @Test
    void shouldConvertStringToCorrectTask() {
        Task savedTask1 = FileBackedTaskManager.fromString("1,TASK,test task title,NEW,test task description");
        Task savedTask2 = FileBackedTaskManager.fromString("2,EPIC,test epic title,NEW,test epic description");
        Task savedTask3 = FileBackedTaskManager.fromString("3,SUBTASK,test subtask1 title,NEW,test subtask1 description,2");
        Task savedTask4 = FileBackedTaskManager.fromString("4,SUBEPIC,test title,NEW,test description");

        assertEquals(TypeTask.TASK, savedTask1.getType(), "При преобразовании строки вернулся не объект Task");
        assertEquals(TypeTask.EPIC, savedTask2.getType(), "При преобразовании строки вернулся не объект Epic");
        assertEquals(TypeTask.SUBTASK, savedTask3.getType(), "При преобразовании строки вернулся не объект Subtask");
        assertNull(savedTask4, "При преобразовании строки вернулся не Null");
    }

    @Test
    void shouldLoadTasksFromFile() throws IOException {
        File tempLoadFile = File.createTempFile("tempload", ".scv");

        try (Writer writer = new FileWriter(tempLoadFile)) {
            writer.write("id,type,title,status,description,epic\n");
            writer.write("1,TASK,test task title,NEW,test task description\n");
            writer.write("2,EPIC,test epic title,NEW,test epic description\n");
            writer.write("3,SUBTASK,test subtask1 title,NEW,test subtask1 description,2");
        }

        TaskManager manager = FileBackedTaskManager.loadFromFile(tempLoadFile);

        // проверяем добавление задач всех типов в Map
        assertEquals(1, manager.getAllTask().size(), "Задача не добавлена из файла");
        assertEquals(1, manager.getAllEpic().size(), "Эпик не добавлен из файла");
        assertEquals(1, manager.getAllSubtask().size(), "Подзадача не добавлена из файла");

        Task task = new Task("test newtask title", "test newtask description");
        manager.addTask(task);

        // проверяем обновление счетчика id до максимального из загруженного списка задач
        assertEquals(4, task.getId(), "Не обновился счетчик id");

        // проверяем связку эпика и подзадачи при загрузке из файла
        assertEquals(2, manager.getSubtask(3).getEpicId(), "Указан неверный id эпика");
        assertNotNull(manager.getEpic(2).getSubtasksIds(), "Нет привязка подзадачи к эпику");
    }
}