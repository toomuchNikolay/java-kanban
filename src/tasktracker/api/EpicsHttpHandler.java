package tasktracker.api;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.exceptions.NotFoundException;
import tasktracker.interfaces.TaskManager;
import tasktracker.storage.Epic;

import java.io.IOException;

public class EpicsHttpHandler extends BaseHttpHandler {
    public EpicsHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void post(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String body = readText(exchange);
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic.getId() == 0)
                    manager.addEpic(epic);
                else
                    manager.updateEpic(epic);
                sendCreated(exchange);
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (NullPointerException exception) {
            sendBadRequest(exchange, "Вместо эпика передан null");
        } catch (NotFoundException exception) {
            sendNotFound(exchange, exception.getMessage());
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }

    @Override
    protected void get(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String jsonString = gson.toJson(manager.getEpicList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Epic epic = manager.getEpic(id);
                    String jsonString = gson.toJson(epic);
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
                manager.clearEpicList();
                String jsonString = gson.toJson(manager.getEpicList());
                sendText(exchange, jsonString);
            } else if (split.length == 3) {
                try {
                    int id = Integer.parseInt(split[2]);
                    Epic epic = manager.removeEpic(id);
                    String jsonString = gson.toJson(epic);
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