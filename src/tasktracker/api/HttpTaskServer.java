package tasktracker.api;

import com.sun.net.httpserver.HttpServer;
import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks/", new TasksHttpHandler(manager));
        server.createContext("/epics/", new EpicsHttpHandler(manager));
        server.createContext("/subtasks/", new SubtasksHttpHandler(manager));
        server.createContext("/history/", new HistoryHttpHandler(manager));
        server.createContext("/prioritized/", new PrioritizedHttpHandler(manager));
    }

    void start() {
        System.out.println("HTTP-сервер запущен на " + PORT + " порту");
        server.start();
    }

    void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен на " + PORT + " порту");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Manager.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
        httpTaskServer.start();
    }
}