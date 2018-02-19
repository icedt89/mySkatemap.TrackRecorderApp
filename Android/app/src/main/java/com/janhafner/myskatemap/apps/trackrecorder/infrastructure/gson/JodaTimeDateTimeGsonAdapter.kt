package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson

import com.google.gson.*
import org.joda.time.DateTime
import java.lang.reflect.Type

internal final class JodaTimeDateTimeGsonAdapter: JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    public override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonPrimitive (src.toString())
    }

    public override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        if (json == null) {
            return DateTime()
        }

        return DateTime(json.asString)
    }
}