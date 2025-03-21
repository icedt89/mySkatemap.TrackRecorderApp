package com.janhafner.myskatemap.apps.activityrecorder.services.couchdb

import com.couchbase.lite.*
import com.couchbase.lite.Dictionary
import com.janhafner.myskatemap.apps.activityrecorder.core.types.*
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal class ActivityInfoConverter {
    public companion object {
        internal fun activityInfoFromCouchDbDictionary(dictionary: Dictionary, id: UUID) : ActivityInfo {
            val result = ActivityInfo()

            result.id = id
            result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))
            result.distance = dictionary.getFloat("distance")
            result.displayName = dictionary.getString("displayName")
            result.startedAt = DateTime(dictionary.getDate("startedAt"))
            result.activityCode = dictionary.getString("activityCode")

            return result
        }
    }
}

internal class ActivityConverter {
    public companion object {
        internal fun activityFromCouchDbDocument(document: Document) : Activity {
            val id = UUID.fromString(document.id)

            val result = Activity(id)

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

            var pausedReason: TrackingPausedReason? = null
            val pausedReasonValue = dictionary.getString("pausedReason")
            if(pausedReasonValue != null) {
                pausedReason = TrackingPausedReason.valueOf(pausedReasonValue)
            }

            var resumedReason: TrackingResumedReason? = null
            val resumedReasonValue = dictionary.getString("resumedReason")
            if(resumedReasonValue != null) {
                resumedReason = TrackingResumedReason.valueOf(resumedReasonValue)
            }

            return StateChangeEntry(at, stateChangeReason, pausedReason, resumedReason)
        }

        private fun locationFromCouchDbDictionary(dictionary: Dictionary) : Location {
            val provider = dictionary.getString("provider")
            val latitude = dictionary.getDouble("latitude")
            val longitude = dictionary.getDouble("longitude")
            val time = DateTime(dictionary.getDate("time"))

            val result = Location(provider, time, latitude, longitude)

            result.accuracy = dictionary.getFloat("accuracy")
            result.altitude = dictionary.getDouble("altitude")
            result.bearing = dictionary.getFloat("bearing")
            result.bearingAccuracyDegrees = dictionary.getFloat("bearingAccuracyDegrees")
            result.speed = dictionary.getFloat("speed")
            result.speedAccuracyMetersPerSecond = dictionary.getFloat("speedAccuracyMetersPerSecond")
            result.verticalAccuracyMeters = dictionary.getFloat("verticalAccuracyMeters")
            result.segmentNumber = dictionary.getInt("segmentNumber")

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

internal fun ActivityInfo.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setString("displayName", this.displayName)
    result.setFloat("distance", this.distance!!)
    result.setInt("recordingTime", this.recordingTime.seconds)
    result.setDate("startedAt", this.startedAt!!.toDate())
    result.setString("activityCode", this.activityCode)

    return result
}

internal fun Activity.toCouchDbDocument(): MutableDocument {
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
        it.time
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
    result.setDate("time", this.time.toDate())
    result.setInt("segmentNumber", this.segmentNumber)

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

    if(this.pausedReason != null){
        result.setString("pausedReason", this.pausedReason.toString())
    }

    if(this.resumedReason != null){
        result.setString("resumedReason", this.resumedReason.toString())
    }

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