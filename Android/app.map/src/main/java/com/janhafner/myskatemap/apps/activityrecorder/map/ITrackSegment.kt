package com.janhafner.myskatemap.apps.activityrecorder.map

import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location

public interface ITrackSegment {
    fun addLocations(locations: List<Location>)

    var polylineColor: Int

    fun remove()

    val hasLocations: Boolean

    var show: Boolean
}