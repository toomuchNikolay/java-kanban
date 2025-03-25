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

class SubtasksHttpHandlerTest {
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
    void shouldMethodPostAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask title", "subtask description", 30, "01.01.2025 12:00", epic.getId());
        String jsonTask = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении подзадачи не вернулся статус 201");
        assertEquals(subtask.getTitle(), manager.getSubtaskList().getFirst().getTitle(),
                "В списке подзадач отсутствует добавленная подзадача");
        assertFalse(epic.getSubtasksIds().isEmpty(),
                "В список связанных с эпиком подзадач не добавился id добавляемой подзадачи");
    }

    @Test
    void shouldMethodPostUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask title", "subtask description", 30, "01.01.2025 12:00", epic.getId());
        manager.addSubtask(subtask);
        Subtask updateSubtask = new Subtask(" update subtask title", "update subtask description", 30, "01.01.2025 12:00", epic.getId());
        updateSubtask.setId(subtask.getId());
        String jsonTask = gson.toJson(updateSubtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении задачи с указанным id не вернулся статус 201");
        assertEquals(updateSubtask.getTitle(), manager.getSubtaskList().getFirst().getTitle(),
                "Не обновилась задача в хранилище");

        Subtask wrongSubtask = new Subtask("wrong title", "wrong description", 30, "01.01.2025 00:00", epic.getId());
        wrongSubtask.setId(111);
        String jsonWrong = gson.toJson(wrongSubtask);

        HttpRequest wrongRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonWrong)).build();
        HttpResponse<String> wrongResponse = client.send(wrongRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, wrongResponse.statusCode(),
                "При передачи подзадачи с несуществующим в хранилище подзадач id не вернулся статус 404");
        assertEquals("В списке отсутствует подзадача с переданным id", wrongResponse.body(),
                "Ошибка в ответе при отправке задачи с несуществующим id");
    }

    @Test
    void shouldMethodPostCheckAvailableTime() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1 title", "subtask1 description", 30, "01.01.2025 12:00", epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2 title", "subtask2 description", 30, "01.01.2025 13:00", epic.getId());
        manager.addSubtask(subtask2);

        Subtask checkSubtask = new Subtask("subtask title", "subtask description", 30, "01.01.2025 12:15", epic.getId());
        String jsonTask = gson.toJson(checkSubtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При добавлении подзадачи не сработала проверка доступности времени и не вернулся статус 406");
        assertFalse(manager.getTaskList().contains(checkSubtask), "Подзадача добавилась на занятое время");

        Subtask updateSubtask = new Subtask(" update subtask title", "update subtask description", 30, "01.01.2025 12:45", epic.getId());
        updateSubtask.setId(subtask1.getId());
        String jsonUpdate = gson.toJson(updateSubtask);

        HttpRequest requestUp = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonUpdate)).build();
        HttpResponse<String> responseUp = client.send(requestUp, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При обновлении подзадачи не сработала проверка доступности времени и не вернулся статус 406");
        assertFalse(manager.getTaskList().contains(checkSubtask), "Подзадача добавилась на занятое время");
    }

    @Test
    void shouldMethodPostNotAddNull() throws IOException, InterruptedException {
        Subtask subtask = null;
        String jsonTask = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При добавлении null в качестве подзадачи не вернулся статус 400");
        assertEquals("Вместо подзадачи передан null", response.body(),
                "Ошибка в ответе при отправке null в качестве задачи");
    }

    @Test
    void shouldMethodPostNotAddSubtaskWithBadEpicId() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask title", "subtask description", 30, "01.01.2025 12:00", 111);
        String jsonTask = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "При добавлении подзадачи с указанием несуществующего id эпика не вернулся статус 404");
        assertEquals("В списке отсутствует эпик с указанным id", response.body(),
                "Ошибка в ответе при отправке подзадачи с несуществующим id эпика");
    }

    @Test
    void shouldMethodGetReturnSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1 title", "subtask1 description", 30, "01.01.2025 12:00", epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2 title", "subtask2 description", 30, "01.01.2025 13:00", epic.getId());
        manager.addSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/subtasks/2/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При поиске подзадачи с указанием id не вернулся статус 200");
        assertEquals(subtask1, gson.fromJson(response.body(), Subtask.class), "В ответе вернулась другая подзадача");

        URI notFoundUrl = URI.create("http://localhost:8080/subtasks/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).GET().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При поиске подзадачи с несуществующим id не вернулся статус 404");
        assertEquals("Подзадача не найдена", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/subtasks/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).GET().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/subtasks/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).GET().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При получении списка всех подзадач не вернулся статус 200");
        assertEquals(manager.getSubtaskList(), gson.fromJson(responseAll.body(), new TypeToken<List<Subtask>>() {
                }.getType()),
                "В ответе список подзадач отличается от фактического");
    }

    @Test
    void shouldMethodDeleteClearSubtasksList() throws IOException, InterruptedException {
        Epic epic = new Epic("epic title", "epic description");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1 title", "subtask1 description", 30, "01.01.2025 12:00", epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2 title", "subtask2 description", 30, "01.01.2025 13:00", epic.getId());
        manager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask("subtask3 title", "subtask3 description", 30, "01.01.2025 14:00", epic.getId());
        manager.addSubtask(subtask3);

        URI url = URI.create("http://localhost:8080/subtasks/3/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При удалении подзадачи с указанием id не вернулся статус 200");
        assertEquals(subtask2, gson.fromJson(response.body(), Subtask.class), "В ответе вернулась другая подзадача");
        assertFalse(manager.getTaskList().contains(subtask2), "Из списка подзадач не удалилась переданная подзадача");

        URI notFoundUrl = URI.create("http://localhost:8080/subtasks/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).DELETE().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При удалении подзадачи с указанием несуществующего id не вернулся статус 404");
        assertEquals("В списке отсутствует подзадача с переданным id", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/subtasks/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).DELETE().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/subtasks/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).DELETE().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При удалении всех задач не вернулся статус 200");
        assertEquals(manager.getSubtaskList(), gson.fromJson(responseAll.body(), new TypeToken<List<Subtask>>() {
                }.getType()),
                "В ответе список задач отличается от фактического");
    }

    @Test
    void shouldReturnBadPathSubtasks() throws IOException, InterruptedException {
        URI urlPost = URI.create("http://localhost:8080/subtasks/1/1/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(HttpRequest.BodyPublishers.ofString("test")).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responsePost.statusCode(),
                "При указании некорректного пути для метода POST не вернулся статус 400");
        assertEquals("Передан некорректный путь", responsePost.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlGet = URI.create("http://localhost:8080/subtasks/1/1/");
        HttpRequest requestGet = HttpRequest.newBuilder().uri(urlGet).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseGet.statusCode(),
                "При указании некорректного пути для метода GET не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseGet.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlDelete = URI.create("http://localhost:8080/subtasks/1/1/");
        HttpRequest requestDelete = HttpRequest.newBuilder().uri(urlDelete).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseDelete.statusCode(),
                "При указании некорректного пути для метода DELETE не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseDelete.body(),
                "Ошибка в ответе при указании некорректного пути");
    }
}