package tasktracker.api;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.interfaces.TaskManager;

import java.io.IOException;

public class PrioritizedHttpHandler extends BaseHttpHandler {
    public PrioritizedHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void post(HttpExchange exchange, String requestPath) throws IOException {
        sendNotAllowed(exchange);
    }

    @Override
    protected void get(HttpExchange exchange, String requestPath) throws IOException {
        try {
            String[] split = requestPath.split("/");
            if (split.length == 2) {
                String jsonString = gson.toJson(manager.getPrioritizedTasks());
                sendText(exchange, jsonString);
            } else {
                sendBadRequest(exchange, "Передан некорректный путь");
            }
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }

    @Override
    protected void delete(HttpExchange exchange, String requestPath) throws IOException {
        sendNotAllowed(exchange);
    }
}