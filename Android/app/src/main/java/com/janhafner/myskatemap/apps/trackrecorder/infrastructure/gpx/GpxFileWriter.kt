package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import java.io.OutputStream
import java.io.OutputStreamWriter

internal final class GpxFileWriter(private val gpxTrackWriter: IGpxTrackWriter) : IGpxFileWriter {
    public override fun writeGpxContent(trackRecordings: List<TrackRecording>, stream: OutputStream) {
        val streamWriter = OutputStreamWriter(stream, Charsets.UTF_8)

        streamWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>")
        streamWriter.write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"Wikipedia\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">")
        streamWriter.write("<metadata>")
        streamWriter.write("<name>?DATEINAME?</name>")
        streamWriter.write("<desc>?KURZE BESCHREIBUNG DES INHALTES (ANZAHL DER TRACKS)?</desc>")
        streamWriter.write("<author>")
        streamWriter.write("<name>?CREATED WITH DUMMY (NAME OF APP)?</name>")
        streamWriter.write("</author>")
        streamWriter.write("</metadata>")

        for(trackRecording in trackRecordings) {
            this.gpxTrackWriter.writeGpxTrack(trackRecording, streamWriter)
        }

        streamWriter.write("</gpx>")

        streamWriter.flush()
    }
}