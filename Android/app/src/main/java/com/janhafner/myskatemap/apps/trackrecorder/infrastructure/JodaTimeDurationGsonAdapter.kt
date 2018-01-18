package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.google.gson.*
import org.joda.time.Duration
import java.lang.reflect.Type

internal final class JodaTimeDurationGsonAdapter : JsonSerializer<Duration>, JsonDeserializer<Duration> {
    public override fun serialize(src: Duration?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if(src == null) {
            return JsonNull.INSTANCE
        }

        return JsonPrimitive (src.millis)
    }

    public override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Duration {
        if(json == null) {
            return Duration.ZERO
        }

        return Duration(json.asLong)
    }
}