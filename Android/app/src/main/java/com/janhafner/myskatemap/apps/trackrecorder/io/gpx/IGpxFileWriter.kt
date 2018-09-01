package com.janhafner.myskatemap.apps.trackrecorder.io.gpx

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import java.io.OutputStream

internal interface IGpxFileWriter {
    fun writeGpxContent(trackRecordings: List<TrackRecording>, stream: OutputStream)
}