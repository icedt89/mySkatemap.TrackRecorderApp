package com.janhafner.myskatemap.apps.activityrecorder.map

import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location

public interface ILatitudeLongitudeBoundsBuilder {
    fun include(location: Location)

    fun build(): LocationBounds
}