package com.janhafner.myskatemap.apps.trackrecorder

import android.support.v4.util.ArrayMap
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TrackRecording(_name: String) {
    public val locations: MutableMap<Int, Location> = ArrayMap<Int, Location>()

    public val attachments: MutableList<Attachment> = ArrayList<Attachment>()

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public var name: String = _name
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