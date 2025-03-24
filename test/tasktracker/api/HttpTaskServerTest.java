package tasktracker.api;

import org.junit.jupiter.api.*;
import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void start() throws IOException {
        TaskManager manager = Manager.getDefault();
        server = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void shouldReturnNotFoundForInvalidPath() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/path/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "При указании несуществующего пути не возвращается статус 404");
    }

    @Test
    void shouldReturnNotAllowedForInvalidMethod() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString("test"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(),
                "При использовании недопустимого метода не возвращается статус 405");
    }
}