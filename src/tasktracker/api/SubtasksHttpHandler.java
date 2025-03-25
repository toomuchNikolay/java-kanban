package tasktracker.api;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.exceptions.NotFoundException;
import tasktracker.exceptions.TaskValidationException;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.Subtask;

import java.io.IOException;

public class SubtasksHttpHandler extends BaseHttpHandler {

    public SubtasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void post(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String body = readText(exchange);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask.getId() == 0)
                    manager.addSubtask(subtask);
                else
                    manager.updateSubtask(subtask);
                sendCreated(exchange);
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (NullPointerException exception) {
            sendBadRequest(exchange, "Вместо подзадачи передан null");
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
                String jsonString = gson.toJson(manager.getSubtaskList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Subtask subtask = manager.getSubtask(id);
                    String jsonString = gson.toJson(subtask);
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
                manager.clearSubtaskList();
                String jsonString = gson.toJson(manager.getSubtaskList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Subtask subtask = manager.removeSubtask(id);
                    String jsonString = gson.toJson(subtask);
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