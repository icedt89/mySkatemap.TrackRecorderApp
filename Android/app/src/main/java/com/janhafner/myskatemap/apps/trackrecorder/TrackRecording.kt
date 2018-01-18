package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.*

internal final class TrackRecording {
    public constructor(name: String) {
        this.name = name
    }

    public var locations: MutableCollection<Location> = ArrayList<Location>()

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public var name: String = ""
        set(value) {
            if(this.isFinished) {
                throw IllegalStateException()
            }

            field = value
        }

    public var trackingStartedAt: DateTime = DateTime.now()
        private set

    public var duration: Duration? = null

    public var trackingFinishedAt: DateTime? = null
        private set

    public fun finish() {
        if(this.isFinished) {
            throw IllegalStateException()
        }

        this.trackingFinishedAt = DateTime.now()
    }

    public val isInvalid: Boolean
        get() = this.name.isNullOrBlank()
}