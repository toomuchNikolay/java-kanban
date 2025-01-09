public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();
        Task task1 = new Task("title task1", "desc task1");
        tm.addTask(task1);
        Task task2 = new Task("title task2", "desc task2");
        tm.addTask(task2);
        Epic epic1 = new Epic("title epic1", "desc epic1");
        tm.addEpic(epic1);
        Epic epic2 = new Epic("title epic2", "desc epic2");
        tm.addEpic(epic2);
        Subtask subtask1 = new Subtask("title subtask1", "desc subtask1", epic1.getId());
        tm.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("title subtask2", "desc subtask2", epic1.getId());
        tm.addSubtask(subtask2);
        Subtask subtask3 = new Subtask("title subtask3", "desc subtask3", epic2.getId());
        tm.addSubtask(subtask3);
        System.out.println("Список эпиков - " + tm.getAllEpic());
        System.out.println("Список задач - " + tm.getAllTask());
        System.out.println("Список подзадач - " + tm.getAllSubtask());
        System.out.println();
        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);
        subtask1.setStatus(Status.DONE);
        subtask3.setStatus(Status.DONE);
        tm.checkEpicStatus(epic1.getId());
        tm.checkEpicStatus(epic2.getId());
        tm.printAllTasks();
        System.out.println();
        tm.deleteTask(task1.getId());
        tm.deleteEpic(epic2.getId());
        tm.printAllTasks();
    }
}