package com.janhafner.myskatemap.apps.trackrecorder.distancecalculation

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location

public interface IDistanceCalculator {
    fun calculateDistance(locations: List<Location>): Float
}