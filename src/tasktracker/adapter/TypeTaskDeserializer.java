package tasktracker.adapter;

import com.google.gson.*;
import tasktracker.storage.*;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;

public class TypeTaskDeserializer implements JsonDeserializer<Task> {
    @Override
    public Task deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        if (object.has("subtasksIds")) {
            return context.deserialize(object, Epic.class);
        } else if (object.has("epicId")) {
            return context.deserialize(object, Subtask.class);
        } else {
            return new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                    .create()
                    .fromJson(object, Task.class);
        }
    }
}