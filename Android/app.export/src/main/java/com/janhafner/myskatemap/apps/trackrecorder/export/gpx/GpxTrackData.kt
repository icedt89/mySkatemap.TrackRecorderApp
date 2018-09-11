package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import org.joda.time.DateTime

public final class GpxTrackData {
    public var startedAt: DateTime = DateTime.now()

    public var gpxTrackLocationData: List<GpxTrackLocationData> = ArrayList()
}