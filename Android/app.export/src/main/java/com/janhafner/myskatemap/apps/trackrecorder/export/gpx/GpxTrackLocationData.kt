package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import org.joda.time.DateTime

public final class GpxTrackLocationData {
    public var altitude: Double? = null

    public var latitude: Double = 0.0

    public var longitude: Double = 0.0

    public var capturedAt: DateTime = DateTime.now()
}