package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data

import android.support.v4.util.ArrayMap
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableArray
import com.couchbase.lite.MutableDocument
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal final class TrackRecording private constructor(public var name: String) {
    private constructor(name: String, id: UUID)
        : this(name) {
        this.id = id
    }

    public var id: UUID = UUID.randomUUID()
        private set

    public var comment: String? = null

    public lateinit var locationProviderTypeName: String

    public val locations: MutableMap<Int, Location> = ArrayMap<Int, Location>()

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public var userProfile: UserProfile? = null

    public val isFinished: Boolean
        get() = this.trackingFinishedAt != null

    public lateinit var trackingStartedAt: DateTime
        private set

    public var trackingFinishedAt: DateTime? = null
        private set

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

    public fun toCouchDbDocument(): MutableDocument {
        val result = MutableDocument(this.id.toString())
        result.setString("documentType", this.javaClass.simpleName)

        result.setString("comment", this.comment)
        result.setString("name", this.name)
        result.setString("locationProviderTypeName", this.locationProviderTypeName)
        result.setInt("recordingTime", this.recordingTime.seconds)
        result.setDate("trackingStartedAt", this.trackingStartedAt.toDate())
        result.setDate("trackingFinishedAt", this.trackingFinishedAt?.toDate())

        if(this.userProfile != null) {
            val fitnessActivityDictionary = this.userProfile!!.toCouchDbDictionary()

            result.setDictionary("userProfile", fitnessActivityDictionary)
        }

        val stateChangeEntriesArray = MutableArray()
        for (stateChangeEntry in this.stateChangeEntries) {
            val stateChangeEntryDictionary = stateChangeEntry.toCouchDbDictionary()

            stateChangeEntriesArray.addDictionary(stateChangeEntryDictionary)
        }

        result.setArray("stateChangeEntries", stateChangeEntriesArray)

        val locationsArray = MutableArray()
        for (location in this.locations.values) {
            val locationDictionary = location.toCouchDbDictionary()

            locationsArray.addDictionary(locationDictionary)
        }

        result.setArray("locations", locationsArray)

        return result
    }

    companion object {
        public fun start(name: String, locationProviderTypeName: String): TrackRecording {
            val result = TrackRecording(name)

            result.locationProviderTypeName = locationProviderTypeName

            result.trackingStartedAt = DateTime.now()
            result.stateChangeEntries.add(StateChangeEntry.start(result.trackingStartedAt))

            return result
        }

        public fun fromCouchDbDocument(document: Document) : TrackRecording {
            val id = UUID.fromString(document.id)

            val result = TrackRecording(document.getString("name"), id)

            result.locationProviderTypeName = document.getString("locationProviderTypeName")
            result.comment = document.getString("comment")
            result.trackingFinishedAt = DateTime(document.getDate("trackingFinishedAt"))
            result.trackingStartedAt = DateTime(document.getDate("trackingStartedAt"))
            result.recordingTime = Period.seconds(document.getInt("recordingTime"))

            val fitnessActivityDictionary = document.getDictionary("userProfile")
            if(fitnessActivityDictionary != null) {
                result.userProfile = UserProfile.fromCouchDbDictionary(fitnessActivityDictionary)
            }

            val stateChangeEntriesArray = document.getArray("stateChangeEntries")
            for (stateChangeEntryDictionary in stateChangeEntriesArray.map { it as Dictionary }) {
                val stateChangeEntry = StateChangeEntry.fromCouchDbDictionary(stateChangeEntryDictionary)

                result.stateChangeEntries.add(stateChangeEntry)
            }

            val locationsArray = document.getArray("locations")
            for (locationDictionary in locationsArray.map { it as Dictionary }) {
                val location = Location.fromCouchDbDictionary(locationDictionary)

                result.locations.put(location.sequenceNumber, location)
            }

            return result
        }

        public fun fromCouchDbDictionary(dictionary: Dictionary, id: UUID) : TrackRecording {
            val result = TrackRecording(dictionary.getString("name"), id)

            result.locationProviderTypeName = dictionary.getString("locationProviderTypeName")
            result.comment = dictionary.getString("comment")
            result.trackingFinishedAt = DateTime(dictionary.getDate("trackingFinishedAt"))
            result.trackingStartedAt = DateTime(dictionary.getDate("trackingStartedAt"))
            result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))

            val fitnessActivityDictionary = dictionary.getDictionary("userProfile")
            if(fitnessActivityDictionary != null) {
                result.userProfile = UserProfile.fromCouchDbDictionary(fitnessActivityDictionary)
            }

            val stateChangeEntriesArray = dictionary.getArray("stateChangeEntries")
            for (stateChangeEntryDictionary in stateChangeEntriesArray.map { it as Dictionary }) {
                val stateChangeEntry = StateChangeEntry.fromCouchDbDictionary(stateChangeEntryDictionary)

                result.stateChangeEntries.add(stateChangeEntry)
            }

            val locationsArray = dictionary.getArray("locations")
            for (locationDictionary in locationsArray.map { it as Dictionary }) {
                val location = Location.fromCouchDbDictionary(locationDictionary)

                result.locations.put(location.sequenceNumber, location)
            }

            return result
        }
    }
}