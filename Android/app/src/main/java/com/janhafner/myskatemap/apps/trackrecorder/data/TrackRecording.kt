package com.janhafner.myskatemap.apps.trackrecorder.data

import android.support.v4.util.ArrayMap
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal final class TrackRecording(public var name: String) {
    private val id: UUID = UUID.randomUUID()

    public val locations: MutableMap<Int, Location> = ArrayMap<Int, Location>()

    public val attachments: MutableList<Attachment> = ArrayList<Attachment>()

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList<StateChangeEntry>()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public lateinit var trackingStartedAt: DateTime
        private set

    public var trackingFinishedAt: DateTime? = null
        private set

    public var recordingTime: Period = Period.ZERO

    public fun finished() {
        this.trackingFinishedAt = DateTime.now()
        val stateChangeEntry = this.stateChangeEntries.last().finished(this.trackingFinishedAt!!)

        this.stateChangeEntries.add(stateChangeEntry)
    }

    public fun paused() {
        val stateChangeEntry = this.stateChangeEntries.last().paused()

        this.stateChangeEntries.add(stateChangeEntry)
    }

    public fun resumed() {
        val stateChangeEntry: StateChangeEntry

        val lastChange = this.stateChangeEntries.lastOrNull()
        if(lastChange == null) {
            this.trackingStartedAt = DateTime.now()
            stateChangeEntry = StateChangeEntry.started(this.trackingStartedAt)
        } else {
            stateChangeEntry = lastChange.resumed()
        }

        this.stateChangeEntries.add(stateChangeEntry)
    }
}