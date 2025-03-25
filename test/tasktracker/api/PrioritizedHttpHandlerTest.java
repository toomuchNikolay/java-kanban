package tasktracker.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;
import tasktracker.storage.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHttpHandlerTest {
    private TaskManager manager;
    private static Gson gson;
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void start() throws IOException {
        manager = Manager.getDefault();
        gson = Manager.getGson();
        server = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void shouldMethodPostReturnNotAllowed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("test")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(),
                "При отправке метода POST для пути /prioritized не вернулся статус 405");
        assertEquals("Метод не поддерживается", response.body(),
                "Ошибка в ответе при отправке метода POST для пути /prioritized");
    }

    @Test
    void shouldMethodGetReturnHistory() throws IOException, InterruptedException {
        Task task = new Task("task title", "task description", 60, "01.01.2025 12:00");
        manager.addTask(task);
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask title", "subtask description", 30, "01.01.2025 13:00", epic.getId());
        manager.addSubtask(subtask);

        URI url = URI.create("http://localhost:8080/prioritized/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "При получении списка приоритизации задач не вернулся статус 200");
        assertEquals(manager.getPrioritizedTasks(), gson.fromJson(response.body(), new TypeToken<List<Task>>() {
                }.getType()),
                "В ответе список истории отличается от фактического");

        URI badUrl = URI.create("http://localhost:8080/prioritized/tasks/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).GET().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче некорректного пути не вернулся статус 400");
        assertEquals("Передан некорректный путь", badResponse.body(),
                "Ошибка в ответе при указании некорректного пути");
    }

    @Test
    void shouldMethodDeleteReturnNotAllowed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(),
                "При отправке метода DELETE для пути /prioritized не вернулся статус 405");
        assertEquals("Метод не поддерживается", response.body(),
                "Ошибка в ответе при отправке метода DELETE для пути /prioritized");
    }
}