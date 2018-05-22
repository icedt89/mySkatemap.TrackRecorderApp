package com.janhafner.myskatemap.apps.trackrecorder.jodatime

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.Period

internal final class JodaTimePeriodMoshiAdapter {
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
}