package com.janhafner.myskatemap.apps.trackrecorder.services.models

import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

public final class TrackRecording (id: UUID = UUID.randomUUID()) {
    public var id: UUID = id
        private set

    public lateinit var locationProviderTypeName: String

    public val locations: MutableList<Location> = ArrayList<Location>()

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public fun addStateChangeEntry(stateChangeEntry: StateChangeEntry) {
        this.stateChangeEntries.add(stateChangeEntry)
    }

    public var userProfile: UserProfile? = null

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public lateinit var trackingStartedAt: DateTime

    public var trackingFinishedAt: DateTime? = null

    public var recordingTime: Period = Period.ZERO

    public val fullTime: Period?
        get() {
            if (this.trackingFinishedAt == null) {
                return null
            }

            return Period(this.trackingStartedAt, this.trackingFinishedAt!!)
        }

    public val pausedTime: Period?
        get() {
            if (this.fullTime == null) {
                return null
            }

            return this.fullTime!!.minus(this.recordingTime)
        }

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
        public fun start(locationProviderTypeName: String): TrackRecording {
            val result = TrackRecording(UUID.randomUUID())

            result.locationProviderTypeName = locationProviderTypeName

            result.trackingStartedAt = DateTime.now()
            result.stateChangeEntries.add(StateChangeEntry.start(result.trackingStartedAt))

            return result
        }
    }
}