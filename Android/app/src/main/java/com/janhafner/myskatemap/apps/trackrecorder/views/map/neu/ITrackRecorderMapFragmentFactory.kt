package com.janhafner.myskatemap.apps.trackrecorder.views.map.neu

import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment

internal interface ITrackRecorderMapFragmentFactory {
    fun createFragment(): TrackRecorderMapFragment
}