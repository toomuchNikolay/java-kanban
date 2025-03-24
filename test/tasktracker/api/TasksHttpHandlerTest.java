package tasktracker.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import tasktracker.interfaces.TaskManager;
import tasktracker.managers.Manager;
import tasktracker.storage.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TasksHttpHandlerTest {
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
    void shouldMethodPostAddTask() throws IOException, InterruptedException {
        Task task = new Task("test title", "test description", 60, "01.01.2025 12:00");
        String jsonTask = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении задачи не вернулся статус 201");
        assertEquals(task.getTitle(), manager.getTaskList().getFirst().getTitle(),
                "В списке задач отсутствует добавленная задача");
    }

    @Test
    void shouldMethodPostUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("test title", "test description", 60, "01.01.2025 12:00");
        manager.addTask(task);

        assertEquals(1, manager.getTaskList().size(), "Задача не добавилась в хранилище");
        assertEquals(task.getTitle(), manager.getTaskList().getFirst().getTitle(),
                "Название задачи в хранилище не совпадает с добавленной");

        Task updateTask = new Task("update test title", "test description", 60, "01.01.2025 12:00");
        updateTask.setId(task.getId());
        String jsonTask = gson.toJson(updateTask);

        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При добавлении задачи с указанным id не вернулся статус 201");
        assertEquals(updateTask.getTitle(), manager.getTaskList().getFirst().getTitle(),
                "Не обновилась задача в хранилище");

        Task wrongTask = new Task("wrong title", "wrong description", 60, "01.01.2025 00:00");
        wrongTask.setId(111);
        String jsonWrong = gson.toJson(wrongTask);

        HttpRequest wrongRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonWrong)).build();
        HttpResponse<String> wrongResponse = client.send(wrongRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, wrongResponse.statusCode(),
                "При передачи задачи с несуществующим в хранилище задач id не вернулся статус 404");
        assertEquals("В списке отсутствует задача с переданным id", wrongResponse.body(),
                "Ошибка в ответе при отправке задачи с несуществующим id");
    }

    @Test
    void shouldMethodPostCheckAvailableTime() throws IOException, InterruptedException {
        Task task1 = new Task("task1 title", "task1 description", 60, "01.01.2025 12:00");
        manager.addTask(task1);
        Task task2 = new Task("task2 title", "task2 description", 60, "01.01.2025 13:00");
        manager.addTask(task2);

        Task checkTask = new Task("check title", "check description", 60, "01.01.2025 11:30");
        String jsonTask = gson.toJson(checkTask);

        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При добавлении задачи не сработала проверка доступности времени и не вернулся статус 406");
        assertFalse(manager.getTaskList().contains(checkTask), "Задача добавилась на занятое время");

        Task updateTask = new Task("update test title", "test description", 60, "01.01.2025 12:30");
        updateTask.setId(task2.getId());
        String jsonUpdate = gson.toJson(updateTask);

        HttpRequest requestUp = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonUpdate)).build();
        HttpResponse<String> responseUp = client.send(requestUp, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При обновлении задачи не сработала проверка доступности времени и не вернулся статус 406");
        assertFalse(manager.getTaskList().contains(checkTask), "Задача добавилась на занятое время");
    }

    @Test
    void shouldMethodPostNotAddNull() throws IOException, InterruptedException {
        Task task = null;
        String jsonTask = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonTask)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При добавлении null в качестве задачи не вернулся статус 400");
        assertEquals("Вместо задачи передан null", response.body(),
                "Ошибка в ответе при отправке null в качестве задачи");
    }

    @Test
    void shouldMethodGetReturnTasks() throws IOException, InterruptedException {
        Task task1 = new Task("task1 title", "task1 description", 60, "01.01.2025 12:00");
        manager.addTask(task1);
        Task task2 = new Task("task2 title", "task2 description", 60, "01.01.2025 13:00");
        manager.addTask(task2);

        URI url = URI.create("http://localhost:8080/tasks/2/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При поиске задачи с указанием id не вернулся статус 200");
        assertEquals(task2, gson.fromJson(response.body(), Task.class), "В ответе вернулась другая задача");

        URI notFoundUrl = URI.create("http://localhost:8080/tasks/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).GET().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При поиске задачи с несуществующим id не вернулся статус 404");
        assertEquals("Задача не найдена", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/tasks/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).GET().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/tasks/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).GET().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При получении списка всех задач не вернулся статус 200");
        assertEquals(manager.getTaskList(), gson.fromJson(responseAll.body(), new TypeToken<List<Task>>() {
                }.getType()),
                "В ответе список задач отличается от фактического");
    }

    @Test
    void shouldMethodDeleteClearTasksList() throws IOException, InterruptedException {
        Task task1 = new Task("task1 title", "task1 description", 60, "01.01.2025 12:00");
        manager.addTask(task1);
        Task task2 = new Task("task2 title", "task2 description", 60, "01.01.2025 13:00");
        manager.addTask(task2);
        Task task3 = new Task("task3 title", "task3 description", 60, "01.01.2025 14:00");
        manager.addTask(task3);

        URI url = URI.create("http://localhost:8080/tasks/2/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При удалении задачи с указанием id не вернулся статус 200");
        assertEquals(task2, gson.fromJson(response.body(), Task.class), "В ответе вернулась другая задача");
        assertFalse(manager.getTaskList().contains(task2), "Из списка задач не удалилась переданная задача");

        URI notFoundUrl = URI.create("http://localhost:8080/tasks/111/");
        HttpRequest notFoundRequest = HttpRequest.newBuilder().uri(notFoundUrl).DELETE().build();
        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, notFoundResponse.statusCode(),
                "При удалении задачи с указанием несуществующего id не вернулся статус 404");
        assertEquals("В списке отсутствует задача с переданным id", notFoundResponse.body(),
                "Ошибка в ответе при отправке несуществующего id");

        URI badUrl = URI.create("http://localhost:8080/tasks/2.2/");
        HttpRequest badRequest = HttpRequest.newBuilder().uri(badUrl).DELETE().build();
        HttpResponse<String> badResponse = client.send(badRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, badResponse.statusCode(),
                "При передаче в качестве id не целого числа не вернулся статус 400");
        assertEquals("Переданный id не является целым числом", badResponse.body(),
                "Ошибка в ответе при отправке не целого числа в качестве id");

        URI urlAll = URI.create("http://localhost:8080/tasks/");
        HttpRequest requestAll = HttpRequest.newBuilder().uri(urlAll).DELETE().build();
        HttpResponse<String> responseAll = client.send(requestAll, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseAll.statusCode(), "При удалении всех задач не вернулся статус 200");
        assertEquals(manager.getTaskList(), gson.fromJson(responseAll.body(), new TypeToken<List<Task>>() {
                }.getType()),
                "В ответе список задач отличается от фактического");
    }

    @Test
    void shouldReturnBadPathTasks() throws IOException, InterruptedException {
        URI urlPost = URI.create("http://localhost:8080/tasks/1/1/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(HttpRequest.BodyPublishers.ofString("test")).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responsePost.statusCode(),
                "При указании некорректного пути для метода POST не вернулся статус 400");
        assertEquals("Передан некорректный путь", responsePost.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlGet = URI.create("http://localhost:8080/tasks/1/1/");
        HttpRequest requestGet = HttpRequest.newBuilder().uri(urlGet).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseGet.statusCode(),
                "При указании некорректного пути для метода GET не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseGet.body(),
                "Ошибка в ответе при указании некорректного пути");

        URI urlDelete = URI.create("http://localhost:8080/tasks/1/1/");
        HttpRequest requestDelete = HttpRequest.newBuilder().uri(urlDelete).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, responseDelete.statusCode(),
                "При указании некорректного пути для метода DELETE не вернулся статус 400");
        assertEquals("Передан некорректный путь", responseDelete.body(),
                "Ошибка в ответе при указании некорректного пути");
    }
}