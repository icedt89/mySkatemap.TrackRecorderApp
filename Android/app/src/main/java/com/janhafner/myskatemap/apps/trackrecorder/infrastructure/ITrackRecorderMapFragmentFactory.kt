package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment

internal interface ITrackRecorderMapFragmentFactory {
    fun createFragment(): TrackRecorderMapFragment
}