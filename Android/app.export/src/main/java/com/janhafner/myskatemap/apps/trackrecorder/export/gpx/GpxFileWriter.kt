package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import android.content.Context
import java.io.OutputStream
import java.io.OutputStreamWriter

final class GpxFileWriter(private val gpxTrackWriter: IGpxTrackWriter, private val context: Context) : IGpxFileWriter {
    public override fun writeGpxContent(gpxData: GpxData, stream: OutputStream) {
        val streamWriter = OutputStreamWriter(stream, Charsets.UTF_8)

        streamWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>")
        streamWriter.write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"${gpxData.creator}\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">")
        streamWriter.write("<metadata>")
        streamWriter.write("<name>${gpxData.name}</name>")
        streamWriter.write("<desc>${gpxData.description}</desc>")
        streamWriter.write("<author>")
        streamWriter.write("<name>${gpxData.author}</name>")
        streamWriter.write("</author>")
        streamWriter.write("</metadata>")

        for(gpxTrackData in gpxData.gpxTrackData) {
            this.gpxTrackWriter.writeGpxTrack(gpxTrackData, streamWriter)
        }

        streamWriter.write("</gpx>")

        streamWriter.flush()
    }
}