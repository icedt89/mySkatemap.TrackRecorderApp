package com.janhafner.myskatemap.apps.trackrecorder.map

public interface ITrackSegment {
    fun addLocations(locations: List<MapLocation>)

    var polylineColor: Int

    fun remove()

    val hasLocations: Boolean

    var show: Boolean
}