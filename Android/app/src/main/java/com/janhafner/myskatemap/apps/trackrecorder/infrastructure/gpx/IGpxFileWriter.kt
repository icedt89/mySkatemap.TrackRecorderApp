package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import java.io.OutputStream

internal interface IGpxFileWriter {
    fun writeGpxContent(trackRecordings: List<TrackRecording>, stream: OutputStream)
}