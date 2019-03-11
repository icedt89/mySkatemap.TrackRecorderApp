package com.janhafner.myskatemap.apps.trackrecorder.map

import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location

public interface ILatitudeLongitudeBoundsBuilder {
    fun include(location: Location)

    fun build(): LocationBounds
}