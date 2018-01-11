package com.janhafner.myskatemap.apps.trackrecorder

import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.location.Location

internal interface ITrackRecorderMap {
    val uiSettings: UiSettings;

    var mapType: Int;

    var trackColor: Int;

    val trackedPath: Iterable<LatLng>;

    fun setRecordedTrack(recordedTrack: Iterable<Location>);
}