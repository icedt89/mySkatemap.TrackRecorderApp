package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson

import com.google.gson.*
import org.joda.time.Period
import java.lang.reflect.Type

internal final class JodaTimePeriodGsonAdapter: JsonSerializer<Period>, JsonDeserializer<Period> {
    public override fun serialize(src: Period?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonPrimitive (src.seconds)
    }

    public override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Period {
        if (json == null) {
            return Period.ZERO
        }

        return Period.seconds(json.asInt)
    }
}