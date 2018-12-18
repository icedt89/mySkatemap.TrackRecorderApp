package com.janhafner.myskatemap.apps.trackrecorder.map

public interface ILatitudeLongitudeBoundsBuilder {
    fun include(location: MapLocation)

    fun build(): MapLocationBounds
}