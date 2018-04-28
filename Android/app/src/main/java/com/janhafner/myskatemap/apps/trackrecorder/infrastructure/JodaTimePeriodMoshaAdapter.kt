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
}