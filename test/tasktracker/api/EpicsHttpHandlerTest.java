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

class EpicsHttpHandlerTest {
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
    void shouldMethodPostAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        String jsonTask = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении эпика не вернулся статус 201");
        assertEquals(epic.getTitle(), manager.getEpicList().getFirst().getTitle(),
                "В списке эпиков отсутствует добавленный эпик");
    }

    @Test
    void shouldMethodPostUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);

        assertEquals(1, manager.getEpicList().size(), "Эпик не добавился в хранилище");
        assertEquals(epic.getTitle(), manager.getEpicList().getFirst().getTitle(),
                "Название эпика в хранилище не совпадает с добавленным");

        Epic updateEpic = new Epic("update test title", "test description");
        updateEpic.setId(epic.getId());
        String jsonTask = gson.toJson(updateEpic);

        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении эпика с указанным id не вернулся статус 201");
        assertEquals(updateEpic.getTitle(), manager.getEpicList().getFirst().getTitle(),
                "Не обновился эпик в хранилище");

        Epic wrongEpic = new Epic("wrong title", "wrong description");
        wrongEpic.setId(111);
        String jsonWrong = gson.toJson(wrongEpic);

        HttpRequest wrongRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonWrong)).build();
        HttpResponse<String> wrongResponse = client.send(wrongRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, wrongResponse.statusCode(),
                "При передачи эпика с несуществующим в хранилище эпиков id не вернулся статус 404");
        assertEquals("В списке отсутствует эпик с переданным id", wrongResponse.body(),
                "Ошибка в ответе при отправке эпика с несуществующим id");
    }

    @Test
    void shouldMethodPostNotAddNull() throws IOException, InterruptedException {
        Epic epic = null;
        String jsonTask = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При добавлении null в качестве эпика не вернулся статус 400");
        assertEquals("Вместо эпика передан null", response.body(),
                "Ошибка в ответе при отправке null в качестве эпика");
    }

    @Test
    void shouldMethodGetReturnEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1 title", "epic1 description");
        manager.addEpic(epic1);
        Epic epic2 = new Epic("epic2 title", "epic2 description");
        manager.addEpic(epic2);

        URI url = URI.create("http://localhost:8080/epics/2/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При поиске эпика с указанием id не вернулся статус 200");
        assertEquals(epic2, gson.fromJson(response.body(), Epic.class), "В ответе вернулась другой эпик");

        URI notFoundUrl = URI.create("http://localhost:8080/epics/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).GET().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При поиске эпика с несуществующим id не вернулся статус 404");
        assertEquals("Эпик не найден", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/epics/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).GET().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/epics/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).GET().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При получении списка всех эпиков не вернулся статус 200");
        assertEquals(manager.getEpicList(), gson.fromJson(responseAll.body(), new TypeToken<List<Epic>>() {
                }.getType()),
                "В ответе список эпиков отличается от фактического");
    }

    @Test
    void shouldMethodDeleteClearEpicsList() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1 title", "epic1 description");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("subtask1 title", "subtask1 description", 30, "01.01.2025 12:00", epic1.getId());
        manager.addSubtask(subtask1);
        Epic epic2 = new Epic("epic2 title", "epic2 description");
        manager.addEpic(epic2);
        Subtask subtask2 = new Subtask("subtask2 title", "subtask2 description", 30, "01.01.2025 13:00", epic2.getId());
        manager.addSubtask(subtask2);
        Epic epic3 = new Epic("epic3 title", "epic3 description");
        manager.addEpic(epic3);
        Subtask subtask3 = new Subtask("subtask3 title", "subtask3 description", 30, "01.01.2025 14:00", epic3.getId());
        manager.addSubtask(subtask3);
        Subtask subtask4 = new Subtask("subtask4 title", "subtask4 description", 30, "01.01.2025 15:00", epic3.getId());
        manager.addSubtask(subtask4);

        URI url = URI.create("http://localhost:8080/epics/3/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При удалении эпика с указанием id не вернулся статус 200");
        assertEquals(epic2, gson.fromJson(response.body(), Epic.class), "В ответе вернулся другой эпик");
        assertFalse(manager.getTaskList().contains(epic2), "Из списка эпиков не удалился переданный эпик");
        assertFalse(manager.getSubtaskList().contains(subtask2),
                "Из списка подзадач не удалилась привязанная к удаленному эпику подзадача");

        URI notFoundUrl = URI.create("http://localhost:8080/epics/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).DELETE().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При удалении эпика с указанием несуществующего id не вернулся статус 404");
        assertEquals("В списке отсутствует эпик с переданным id", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/epics/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).DELETE().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/epics/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).DELETE().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При удалении всех эпиков не вернулся статус 200");
        assertEquals(manager.getEpicList(), gson.fromJson(responseAll.body(), new TypeToken<List<Epic>>() {
                }.getType()),
                "В ответе список эпиков отличается от фактического");
        assertTrue(manager.getSubtaskList().isEmpty(), "После удаления всех эпиков не очистился список подзадач");
    }

    @Test
    void shouldReturnBadPathEpics() throws IOException, InterruptedException {
        URI urlPost = URI.create("http://localhost:8080/epics/1/1/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(HttpRequest.BodyPublishers.ofString("test")).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responsePost.statusCode(),
                "При указании некорректного пути для метода POST не вернулся статус 400");
        assertEquals("Передан некорректный путь", responsePost.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlGet = URI.create("http://localhost:8080/epics/1/1/");
        HttpRequest requestGet = HttpRequest.newBuilder().uri(urlGet).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseGet.statusCode(),
                "При указании некорректного пути для метода GET не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseGet.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlDelete = URI.create("http://localhost:8080/epics/1/1/");
        HttpRequest requestDelete = HttpRequest.newBuilder().uri(urlDelete).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseDelete.statusCode(),
                "При указании некорректного пути для метода DELETE не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseDelete.body(),
                "Ошибка в ответе при указании некорректного пути");
    }
}