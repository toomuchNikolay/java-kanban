import TaskTracker.interfaces.*;
import TaskTracker.managers.Manager;
import TaskTracker.storage.*;

public static void main(String[] args) {
    TaskManager taskManager = Manager.getDefault();
    Task task1 = new Task("title task1", "desc task1");
    taskManager.addTask(task1);
    Task task2 = new Task("title task2", "desc task2");
    taskManager.addTask(task2);
    Task task3 = new Task("title task3", "desc task3");
    taskManager.addTask(task3);
    Epic epic1 = new Epic("title epic1", "desc epic1");
    taskManager.addEpic(epic1);
    Epic epic2 = new Epic("title epic2", "desc epic2");
    taskManager.addEpic(epic2);
    Subtask subtask1 = new Subtask("title subtask1", "desc subtask1", epic1.getId());
    taskManager.addSubtask(subtask1);
    Subtask subtask2 = new Subtask("title subtask2", "desc subtask2", epic1.getId());
    taskManager.addSubtask(subtask2);
    Subtask subtask3 = new Subtask("title subtask3", "desc subtask3", epic2.getId());
    taskManager.addSubtask(subtask3);

    printAllTasks(taskManager);
}

private static void printAllTasks(TaskManager taskManager) {
    System.out.println("Задачи:");
    for (Task task : taskManager.getAllTask()) {
        System.out.println(task);
    }
    System.out.println("Эпики:");
    for (Task epic : taskManager.getAllEpic()) {
        System.out.println(epic);

        for (Task task : taskManager.getAllSubtaskByEpic(epic.getId())) {
            System.out.println("--> " + task);
        }
    }
    System.out.println("Подзадачи:");
    for (Task subtask : taskManager.getAllSubtask()) {
        System.out.println(subtask);
    }

    System.out.println("История:");
    for (Task task : taskManager.getHistory()) {
        System.out.println(task);
    }
}