package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import com.janhafner.myskatemap.apps.trackrecorder.common.formatDefault
import java.io.Writer

final class GpxTrackWriter : IGpxTrackWriter {
    public override fun writeGpxTrack(gpxTrackData: GpxTrackData, streamWriter: Writer) {
        streamWriter.write("<trk>")

        streamWriter.write("<name>${gpxTrackData.startedAt.formatDefault()}</name>")

        streamWriter.write("<trkseg>")

        for(location in gpxTrackData.gpxTrackLocationData) {
            streamWriter.write("<trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\">")

            if(location.altitude != null) {
                streamWriter.write("<ele>${location.altitude}</ele>")
            }

            streamWriter.write("<time>${location.capturedAt}</time>")
            streamWriter.write("</trkpt>")
        }

        streamWriter.write("</trkseg>")
        streamWriter.write("</trk>")

        streamWriter.flush()
    }
}