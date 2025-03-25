package tasktracker.api;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.exceptions.NotFoundException;
import tasktracker.exceptions.TaskValidationException;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.Task;

import java.io.IOException;

public class TasksHttpHandler extends BaseHttpHandler {

    public TasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void post(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String body = readText(exchange);
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == 0)
                    manager.addTask(task);
                else
                    manager.updateTask(task);
                sendCreated(exchange);
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (NullPointerException exception) {
            sendBadRequest(exchange, "Вместо задачи передан null");
        } catch (NotFoundException exception) {
            sendNotFound(exchange, exception.getMessage());
        } catch (TaskValidationException exception) {
            sendHasInteractions(exchange);
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }

    @Override
    protected void get(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String jsonString = gson.toJson(manager.getTaskList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Task task = manager.getTask(id);
                    String jsonString = gson.toJson(task);
                    sendText(exchange, jsonString);
                } catch (NumberFormatException exception) {
                    sendBadRequest(exchange, "Переданный id не является целым числом");
                } catch (NotFoundException exception) {
                    sendNotFound(exchange, exception.getMessage());
                }
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }

    @Override
    protected void delete(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                manager.clearTaskList();
                String jsonString = gson.toJson(manager.getTaskList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Task task = manager.removeTask(id);
                    String jsonString = gson.toJson(task);
                    sendText(exchange, jsonString);
                } catch (NumberFormatException exception) {
                    sendBadRequest(exchange, "Переданный id не является целым числом");
                } catch (NotFoundException exception) {
                    sendNotFound(exchange, exception.getMessage());
                }
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }
}