package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import java.io.OutputStream
import java.io.OutputStreamWriter

internal final class GpxTrackWriter : IGpxTrackWriter {
    public override fun writeGpxTrack(trackRecording: TrackRecording, stream: OutputStream) {
        val streamWriter = OutputStreamWriter(stream, Charsets.UTF_8)

        streamWriter.write("<trk>")
        streamWriter.write("<name>${trackRecording.name}</name>")
        streamWriter.write("<desc>${trackRecording.comment}</desc>")
        streamWriter.write("<trkseg>")

        for(point in trackRecording.locations.values) {
            streamWriter.write("<trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
            streamWriter.write("<ele>${point.altitude}</ele>")
            streamWriter.write("<time>${point.capturedAt}</time>")
            streamWriter.write("</trkpt>")
        }

        streamWriter.write("</trkseg>")
        streamWriter.write("</trk>")

        streamWriter.flush()
    }
}