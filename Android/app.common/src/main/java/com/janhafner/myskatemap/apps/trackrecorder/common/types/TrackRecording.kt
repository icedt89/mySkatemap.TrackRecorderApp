package com.janhafner.myskatemap.apps.trackrecorder.common.types

import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*
import kotlin.collections.ArrayList

public final class TrackRecording (id: UUID = UUID.randomUUID()) {
    public var id: UUID = id
        private set

    private val locationEntries: MutableList<Location> = ArrayList<Location>()
    public val locations: List<Location>
        get() = this.locationEntries

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public fun addStateChangeEntry(stateChangeEntry: StateChangeEntry) {
        this.stateChangeEntries.add(stateChangeEntry)
    }

    public fun addLocation(location: Location) {
        this.locationEntries.add(location)
    }

    public fun addLocations(locations: Iterable<Location>) {
        this.locationEntries.addAll(locations)
    }

    public var userProfile: UserProfile? = null

    public lateinit var startedAt: DateTime

    public var finishedAt: DateTime? = null

    public var recordingTime: Period = Period.ZERO

    public fun finish() {
        this.finishedAt = DateTime.now()
        val stateChangeEntry = this.stateChangeEntries.last().finish(this.finishedAt!!)

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
        public fun start(): TrackRecording {
            val result = TrackRecording(UUID.randomUUID())

            result.startedAt = DateTime.now()
            result.stateChangeEntries.add(StateChangeEntry.start(result.startedAt))

            return result
        }
    }
}