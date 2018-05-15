package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import java.io.Writer

internal final class GpxTrackWriter : IGpxTrackWriter {
    public override fun writeGpxTrack(trackRecording: TrackRecording, streamWriter: Writer) {
        streamWriter.write("<trk>")
        streamWriter.write("<name>${trackRecording.name}</name>")

        if(trackRecording.comment != null) {
            streamWriter.write("<desc>${trackRecording.comment}</desc>")
        }

        streamWriter.write("<trkseg>")

        for(point in trackRecording.locations.values) {
            streamWriter.write("<trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")

            if(point.altitude != null) {
                streamWriter.write("<ele>${point.altitude}</ele>")
            }

            streamWriter.write("<time>${point.capturedAt}</time>")
            streamWriter.write("</trkpt>")
        }

        streamWriter.write("</trkseg>")
        streamWriter.write("</trk>")

        streamWriter.flush()
    }
}