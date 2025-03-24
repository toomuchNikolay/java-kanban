package tasktracker.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tasktracker.adapter.DurationTypeAdapter;
import tasktracker.adapter.LocalDateTimeTypeAdapter;
import tasktracker.adapter.TypeTaskDeserializer;
import tasktracker.interfaces.*;
import tasktracker.storage.Task;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public final class Manager {
    private Manager() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getDefault(Path path) {
        return FileBackedTaskManager.loadFromFile(path.toFile());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Task.class, new TypeTaskDeserializer())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }
}