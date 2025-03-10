package tasktracker;

import tasktracker.interfaces.*;
import tasktracker.managers.Manager;
import tasktracker.storage.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Manager.getDefault();

        Task task1 = new Task("title task1", "desc task1", 60, "01.01.2025 00:00");
        taskManager.addTask(task1);
        Task task2 = new Task("title task2", "desc task2", 60, "01.01.2025 10:00");
        taskManager.addTask(task2);
        Epic epic1 = new Epic("title epic1", "desc epic1");
        taskManager.addEpic(epic1);
        Epic epic2 = new Epic("title epic2", "desc epic2");
        taskManager.addEpic(epic2);
        Subtask subtask1 = new Subtask("title subtask1", "desc subtask1", 30,
                "01.01.2025 01:00", epic1.getId());
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("title subtask2", "desc subtask2", 30,
                "01.01.2025 02:00", epic1.getId());
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask("title subtask3", "desc subtask3", 30,
                "01.01.2025 03:00", epic2.getId());
        taskManager.addSubtask(subtask3);

        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        for (Task task : taskManager.getTaskList()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : taskManager.getEpicList()) {
            System.out.println(epic);

            for (Task task : taskManager.getAllSubtaskByEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : taskManager.getSubtaskList()) {
            System.out.println(subtask);
        }
        System.out.println();
        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
        System.out.println("-".repeat(100));
    }
}