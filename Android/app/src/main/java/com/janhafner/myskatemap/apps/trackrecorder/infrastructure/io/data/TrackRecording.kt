package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import android.support.v4.util.ArrayMap
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal final class TrackRecording private constructor(public var name: String) {
    public val id: UUID = UUID.randomUUID()

    public var comment: String = ""

    public val locations: MutableMap<Int, Location> = ArrayMap<Int, Location>()

    public val attachments: MutableList<Attachment> = ArrayList()

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public lateinit var trackingStartedAt: DateTime

        private set

    public var trackingFinishedAt: DateTime? = null
        private set

    public var recordingTime: Period = Period.ZERO

    public fun finish() {
        this.trackingFinishedAt = DateTime.now()
        val stateChangeEntry = this.stateChangeEntries.last().finish(this.trackingFinishedAt!!)

        this.stateChangeEntries.add(stateChangeEntry)
    }

    public fun pause() {
        val stateChangeEntry = this.stateChangeEntries.last().pause()

        this.stateChangeEntries.add(stateChangeEntry)
    }

    public fun resume() {
        val stateChangeEntry = this.stateChangeEntries.last().resume()

        this.stateChangeEntries.add(stateChangeEntry)
    }

    companion object {
        public fun start(name: String): TrackRecording {
            val result = TrackRecording(name)

            result.trackingStartedAt = DateTime.now()
            result.stateChangeEntries.add(StateChangeEntry.start(result.trackingStartedAt))

            return result
        }
    }
}