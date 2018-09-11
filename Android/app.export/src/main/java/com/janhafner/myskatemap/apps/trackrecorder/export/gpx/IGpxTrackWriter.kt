package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import java.io.Writer

interface IGpxTrackWriter {
    fun writeGpxTrack(gpxTrackData: GpxTrackData, streamWriter: Writer)
}

