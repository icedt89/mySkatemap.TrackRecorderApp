package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.jodatime

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

internal final class UuidMoshaAdapter {
    @ToJson
    public fun toJson(value: UUID?): String? {
        if(value != null) {
            return value.toString()
        } else {
            return null
        }
    }

    @FromJson
    public fun fromJson(value: String?): UUID? {
        if(value != null) {
            return UUID.fromString(value)
        } else {
            return null
        }
    }
}