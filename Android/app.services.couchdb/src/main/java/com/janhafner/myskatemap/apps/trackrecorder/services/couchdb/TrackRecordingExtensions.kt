package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.couchbase.lite.Dictionary
import com.janhafner.myskatemap.apps.trackrecorder.common.Sex
import com.janhafner.myskatemap.apps.trackrecorder.services.models.*
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal fun trackRecordingFromCouchDbDocument(document: Document) : TrackRecording {
    val id = UUID.fromString(document.id)

    val result = TrackRecording(id)

    result.locationProviderTypeName = document.getString("locationProviderTypeName")
    result.trackingFinishedAt = DateTime(document.getDate("trackingFinishedAt"))
    result.trackingStartedAt = DateTime(document.getDate("trackingStartedAt"))
    result.recordingTime = Period.seconds(document.getInt("recordingTime"))

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

        result.locations.add(location)
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

    result.locationProviderTypeName = dictionary.getString("locationProviderTypeName")
    result.trackingFinishedAt = DateTime(dictionary.getDate("trackingFinishedAt"))
    result.trackingStartedAt = DateTime(dictionary.getDate("trackingStartedAt"))
    result.recordingTime = Period.seconds(dictionary.getInt("recordingTime"))

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

        result.locations.add(location)
    }

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

internal fun TrackRecording.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setString("locationProviderTypeName", this.locationProviderTypeName)
    result.setInt("recordingTime", this.recordingTime.seconds)
    result.setDate("trackingStartedAt", this.trackingStartedAt.toDate())
    result.setDate("trackingFinishedAt", this.trackingFinishedAt?.toDate())

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

public fun locationFromCouchDbDictionary(dictionary: Dictionary) : Location {
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

private fun UserProfile.toCouchDbDictionary() : Dictionary {
    val result = MutableDictionary()

    result.setInt("age", this.age)
    result.setString("metActivityCode", this.metActivityCode)
    result.setFloat("weightInKilograms", this.weightInKilograms)
    result.setInt("heightInCentimeters", this.heightInCentimeters)
    result.setString("sex", this.sex.toString())

    return result
}

private fun userProfileFromCouchDbDictionary(dictionary : Dictionary) : UserProfile {
    val age = dictionary.getInt("age")
    val metActivityCode = dictionary.getString("metActivityCode")
    val weightInKilograms = dictionary.getFloat("weightInKilograms")
    val heightInCentimeters = dictionary.getInt("heightInCentimeters")
    val sex = Sex.valueOf(dictionary.getString("sex"))

    return UserProfile(age, metActivityCode, weightInKilograms, heightInCentimeters, sex)
}