package com.janhafner.myskatemap.apps.trackrecorder.io.gpx

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import java.io.Writer

internal interface IGpxTrackWriter {
    fun writeGpxTrack(trackRecording: TrackRecording, streamWriter: Writer)
}

