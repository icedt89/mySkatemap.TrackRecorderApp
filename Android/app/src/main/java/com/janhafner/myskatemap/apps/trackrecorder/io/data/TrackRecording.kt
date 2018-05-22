package com.janhafner.myskatemap.apps.trackrecorder.io.data

import android.support.v4.util.ArrayMap
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableArray
import com.couchbase.lite.MutableDocument
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal final class TrackRecording private constructor(public var name: String) {
    public val id: UUID = UUID.randomUUID()

    public var comment: String? = null

    public lateinit var locationProviderTypeName: String

    public val locations: MutableMap<Int, Location> = ArrayMap<Int, Location>()

    public val attachments: MutableList<Attachment> = ArrayList()

    private val stateChangeEntries: MutableList<StateChangeEntry> = ArrayList()
    public val stateChanges: List<StateChangeEntry>
        get() = this.stateChangeEntries

    public var fitnessActivity: FitnessActivity? = null

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

        result.setString("comment", this.comment)
        result.setString("name", this.name)
        result.setString("locationProviderTypeName", this.locationProviderTypeName)
        result.setInt("recordingTime", this.recordingTime.seconds)
        result.setDate("trackingStartedAt", this.trackingStartedAt.toDate())
        result.setDate("trackingFinishedAt", this.trackingFinishedAt?.toDate())

        if(this.fitnessActivity != null) {
            val fitnessActivityDictionary = this.fitnessActivity!!.toCouchDbDictionary()

            result.setDictionary("fitnessActivity", fitnessActivityDictionary)
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

        val attachmentsArray = MutableArray()
        for (attachment in this.attachments) {
            val attachmentDictionary = attachment.toCouchDbDictionary()

            attachmentsArray.addDictionary(attachmentDictionary)
        }

        result.setArray("attachments", attachmentsArray)

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
            val result = TrackRecording(document.getString("name"))

            result.locationProviderTypeName = document.getString("locationProviderTypeName")
            result.comment = document.getString("comment")
            result.trackingFinishedAt = DateTime(document.getDate("trackingFinishedAt"))
            result.trackingStartedAt = DateTime(document.getDate("trackingStartedAt"))
            result.recordingTime = Period.seconds(document.getInt("recordingTime"))

            val fitnessActivityDictionary = document.getDictionary("fitnessActivity")
            if(fitnessActivityDictionary != null) {
                result.fitnessActivity = FitnessActivity.fromCouchDbDictionary(fitnessActivityDictionary)
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

            val attachmentsArray = document.getArray("attachments")
            for (attachmentDictionary in attachmentsArray.map { it as Dictionary}) {
                val attachment = Attachment.fromCouchDbDictionary(attachmentDictionary)

                result.attachments.add(attachment)
            }

            return result
        }

        public fun fromCouchDbDictionary(dictionary: Dictionary) : TrackRecording {
            val result = TrackRecording(dictionary.getString("name"))

            result.locationProviderTypeName = dictionary.getString("locationProviderTypeName")
            result.comment = dictionary.getString("comment")
            result.trackingFinishedAt = DateTime(dictionary.getDate("trackingFinishedAt"))
            result.trackingStartedAt = DateTime(dictionary.getDate("trackingStartedAt"))
            result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))

            val fitnessActivityDictionary = dictionary.getDictionary("fitnessActivity")
            if(fitnessActivityDictionary != null) {
                result.fitnessActivity = FitnessActivity.fromCouchDbDictionary(fitnessActivityDictionary)
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

            val attachmentsArray = dictionary.getArray("attachments")
            for (attachmentDictionary in attachmentsArray.map { it as Dictionary}) {
                val attachment = Attachment.fromCouchDbDictionary(attachmentDictionary)

                result.attachments.add(attachment)
            }

            return result
        }
    }
}