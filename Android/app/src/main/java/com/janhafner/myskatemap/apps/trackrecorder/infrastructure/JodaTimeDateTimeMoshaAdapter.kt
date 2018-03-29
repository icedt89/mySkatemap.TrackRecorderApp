package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime

internal final class JodaTimeDateTimeMoshaAdapter {
    @ToJson
    public fun toJson(value: DateTime?): String? {
        if(value != null) {
            return value.toString()
        } else {
            return null
        }
    }

    @FromJson
    public fun fromJson(value: String?): DateTime? {
        if(value != null) {
            return DateTime(value)
        } else {
            return null
        }
    }

    /*
    public fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonPrimitive (src.toString())
    }

    public fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime {
        if (json == null) {
            return DateTime()
        }

        return DateTime(json.asString)
    }
    */
}