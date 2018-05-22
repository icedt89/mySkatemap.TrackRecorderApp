package com.janhafner.myskatemap.apps.trackrecorder.jodatime

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime

internal final class JodaTimeDateTimeMoshiAdapter {
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
}