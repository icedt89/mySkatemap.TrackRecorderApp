package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.Period


internal final class JodaTimePeriodMoshaAdapter {
    @ToJson
    public fun toJson(value: Period?): String? {
        if(value != null) {
            return value.seconds.toString()
        } else {
            return null
        }
    }

    @FromJson
    public fun fromJson(value: String?): Period? {
        if(value != null) {
            return Period.seconds(value.toInt())
        } else {
            return null
        }
    }

    /*
    public fun serialize(src: Period?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonPrimitive (src.seconds)
    }

    public fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Period {
        if (json == null) {
            return Period.ZERO
        }

        return Period.seconds(json.asInt)
    }
    */
}