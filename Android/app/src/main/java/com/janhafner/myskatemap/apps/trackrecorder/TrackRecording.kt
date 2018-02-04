package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TrackRecording {
    public constructor(name: String) {
        this.name = name
    }

    public var locations: HashMap<Int, Location> = HashMap<Int, Location>()

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public var name: String = ""
        set(value) {
            if (this.isFinished) {
                throw IllegalStateException()
            }

            field = value
        }

    public var trackingStartedAt: DateTime = DateTime.now()
        private set

    public var recordingTime: Period = Period.ZERO

    public var trackingFinishedAt: DateTime? = null
        private set

    public fun finish() {
        if (this.isFinished) {
            throw IllegalStateException()
        }

        this.trackingFinishedAt = DateTime.now()
    }
}