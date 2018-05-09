package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import java.io.Writer

internal interface IGpxTrackWriter {
    fun writeGpxTrack(trackRecording: TrackRecording, streamWriter: Writer)
}

