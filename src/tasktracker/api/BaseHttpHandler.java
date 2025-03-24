package tasktracker.api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String POST = "POST";
    protected static final String GET = "GET";
    protected static final String DELETE = "DELETE";
    protected final TaskManager manager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
        gson = Manager.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        switch (requestMethod) {
            case POST -> post(exchange, requestPath);
            case GET -> get(exchange, requestPath);
            case DELETE -> delete(exchange, requestPath);
            default -> sendNotAllowed(exchange);
        }
    }

    protected abstract void post(HttpExchange exchange, String requestPath) throws IOException;

    protected abstract void get(HttpExchange exchange, String requestPath) throws IOException;

    protected abstract void delete(HttpExchange exchange, String requestPath) throws IOException;

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, 0);
        exchange.close();
    }

    protected void sendBadRequest(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotAllowed(HttpExchange exchange) throws IOException {
        String response = "Метод не поддерживается";
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String response = "Добавляемая задача пересекается с существующими";
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_ACCEPTABLE, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        String response = "Внутренняя ошибка сервера";
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}