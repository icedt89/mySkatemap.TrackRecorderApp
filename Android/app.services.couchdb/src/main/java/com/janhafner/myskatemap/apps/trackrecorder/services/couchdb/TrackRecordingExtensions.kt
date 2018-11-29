package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.couchbase.lite.Dictionary
import com.janhafner.myskatemap.apps.trackrecorder.common.types.*
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal class TrackInfoConverter {
    public companion object {
        internal fun trackInfoFromCouchDbDictionary(dictionary: Dictionary, id: UUID) : TrackInfo {
            val result = TrackInfo()

            result.id = id
            result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))
            result.distance = dictionary.getFloat("distance")
            result.displayName = dictionary.getString("displayName")

            return result
        }
    }
}

internal class TrackRecordingConverter {
    public companion object {
        internal fun trackRecordingFromCouchDbDocument(document: Document) : TrackRecording {
            val id = UUID.fromString(document.id)

            val result = TrackRecording(id)

            result.finishedAt = DateTime(document.getDate("finishedAt"))
            result.startedAt = DateTime(document.getDate("startedAt"))
            result.recordingTime = Period.seconds(document.getInt("recordingTime"))
            result.activityCode = document.getString("activityCode")

            val fitnessActivityDictionary = document.getDictionary("fitnessActivity")
            if(fitnessActivityDictionary != null) {
                result.userProfile = userProfileFromCouchDbDictionary(fitnessActivityDictionary)
            }

            val stateChangeEntriesArray = document.getArray("stateChangeEntries")
            for (stateChangeEntryDictionary in stateChangeEntriesArray.map { it as Dictionary }) {
                val stateChangeEntry = stateChangeEntryFromCouchDbDictionary(stateChangeEntryDictionary)

                result.addStateChangeEntry(stateChangeEntry)
            }

            val locationsArray = document.getArray("locations")
            for (locationDictionary in locationsArray.map { it as Dictionary }) {
                val location = locationFromCouchDbDictionary(locationDictionary)

                result.addLocations(listOf(location))
            }

            return result
        }

        private fun stateChangeEntryFromCouchDbDictionary(dictionary: Dictionary) : StateChangeEntry {
            val at = DateTime(dictionary.getDate("at"))
            val stateChangeReason = StateChangeReason.valueOf(dictionary.getString("stateChangeReason"))

            return StateChangeEntry(at, stateChangeReason)
        }

        internal fun trackRecordingFromCouchDbDictionary(dictionary: Dictionary, id: UUID) : TrackRecording {
            val result = TrackRecording(id)

            result.finishedAt = DateTime(dictionary.getDate("finishedAt"))
            result.startedAt = DateTime(dictionary.getDate("startedAt"))
            result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))
            result.activityCode = dictionary.getString("activityCode")

            val fitnessActivityDictionary = dictionary.getDictionary("fitnessActivity")
            if(fitnessActivityDictionary != null) {
                result.userProfile = userProfileFromCouchDbDictionary(fitnessActivityDictionary)
            }

            val stateChangeEntriesArray = dictionary.getArray("stateChangeEntries")
            for (stateChangeEntryDictionary in stateChangeEntriesArray.map { it as Dictionary }) {
                val stateChangeEntry = stateChangeEntryFromCouchDbDictionary(stateChangeEntryDictionary)

                result.addStateChangeEntry(stateChangeEntry)
            }

            val locationsArray = dictionary.getArray("locations")
            for (locationDictionary in locationsArray.map { it as Dictionary }) {
                val location = locationFromCouchDbDictionary(locationDictionary)

                result.addLocations(listOf(location))
            }

            return result
        }

        private fun locationFromCouchDbDictionary(dictionary: Dictionary) : Location {
            val result = Location()

            result.provider = dictionary.getString("provider")
            result.latitude = dictionary.getDouble("latitude")
            result.longitude = dictionary.getDouble("longitude")
            result.capturedAt = DateTime(dictionary.getDate("capturedAt"))
            result.accuracy = dictionary.getFloat("accuracy")
            result.altitude = dictionary.getDouble("altitude")
            result.bearing = dictionary.getFloat("bearing")
            result.bearingAccuracyDegrees = dictionary.getFloat("bearingAccuracyDegrees")
            result.speed = dictionary.getFloat("speed")
            result.speedAccuracyMetersPerSecond = dictionary.getFloat("speedAccuracyMetersPerSecond")
            result.verticalAccuracyMeters = dictionary.getFloat("verticalAccuracyMeters")

            return result
        }

        private fun userProfileFromCouchDbDictionary(dictionary : Dictionary) : UserProfile {
            val age = dictionary.getInt("age")
            val weightInKilograms = dictionary.getFloat("weightInKilograms")
            val heightInCentimeters = dictionary.getInt("heightInCentimeters")
            val sex = Sex.valueOf(dictionary.getString("sex"))

            return UserProfile(age, weightInKilograms, heightInCentimeters, sex)
        }
    }
}

internal fun TrackInfo.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setString("displayName", this.displayName)
    result.setFloat("distance", this.distance!!)
    result.setInt("recordingTime", this.recordingTime.seconds)

    return result
}

internal fun TrackRecording.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setInt("recordingTime", this.recordingTime.seconds)
    result.setDate("startedAt", this.startedAt.toDate())
    result.setDate("finishedAt", this.finishedAt?.toDate())
    result.setString("activityCode", this.activityCode)

    if(this.userProfile != null) {
        val fitnessActivityDictionary = this.userProfile!!.toCouchDbDictionary()

        result.setDictionary("userProfile", fitnessActivityDictionary)
    }

    val stateChangeEntriesArray = MutableArray()
    for (stateChangeEntry in this.stateChanges) {
        val stateChangeEntryDictionary = stateChangeEntry.toCouchDbDictionary()

        stateChangeEntriesArray.addDictionary(stateChangeEntryDictionary)
    }

    result.setArray("stateChangeEntries", stateChangeEntriesArray)

    val locationsArray = MutableArray()
    for (location in this.locations.sortedBy {
        it.capturedAt
    }) {
        val locationDictionary = location.toCouchDbDictionary()

        locationsArray.addDictionary(locationDictionary)
    }

    result.setArray("locations", locationsArray)

    return result
}


private fun Location.toCouchDbDictionary() : Dictionary {
    val result = MutableDictionary()

    result.setString("provider", this.provider)
    result.setDouble("latitude", this.latitude)
    result.setDouble("longitude", this.longitude)
    result.setDate("capturedAt", this.capturedAt.toDate())

    if(this.accuracy != null) {
        result.setFloat("accuracy", this.accuracy!!)
    }

    if(this.altitude != null) {
        result.setDouble("altitude", this.altitude!!)
    }

    if(this.bearing != null) {
        result.setFloat("bearing", this.bearing!!)
    }

    if(this.bearingAccuracyDegrees != null) {
        result.setFloat("bearingAccuracyDegrees", this.bearingAccuracyDegrees!!)
    }

    if(this.speed != null) {
        result.setFloat("speed", this.speed!!)
    }

    if(this.speedAccuracyMetersPerSecond != null) {
        result.setFloat("speedAccuracyMetersPerSecond", this.speedAccuracyMetersPerSecond!!)
    }

    if(this.verticalAccuracyMeters != null) {
        result.setFloat("verticalAccuracyMeters", this.verticalAccuracyMeters!!)
    }

    return result
}

private fun StateChangeEntry.toCouchDbDictionary(): Dictionary {
    val result = MutableDictionary()

    result.setDate("at", this.at.toDate())
    result.setString("stateChangeReason", this.stateChangeReason.toString())

    return result
}


private fun UserProfile.toCouchDbDictionary() : Dictionary {
    val result = MutableDictionary()

    result.setInt("age", this.age)
    result.setFloat("weightInKilograms", this.weightInKilograms)
    result.setInt("heightInCentimeters", this.heightInCentimeters)
    result.setString("sex", this.sex.toString())

    return result
}