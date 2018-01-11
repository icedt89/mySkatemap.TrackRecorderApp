package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.google.gson.*
import org.joda.time.DateTime
import java.lang.reflect.Type

internal final class JodaTimeGsonAdapter : JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive (src.toString());
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        return DateTime(json!!.asJsonPrimitive.getAsString());
    }
}
/*
private class DateTimeSerializer implements JsonSerializer<DateTime> {
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive (src.toString());
    }
}
private class DateTimeDeserializer implements JsonDeserializer<DateTime> {
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
        return new DateTime(json.getAsJsonPrimitive().getAsString());
    }
}
        */