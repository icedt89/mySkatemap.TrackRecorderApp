package com.janhafner.myskatemap.apps.trackrecorder.io.gpx

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfile
import org.joda.time.DateTime
import java.io.OutputStream
import java.io.OutputStreamWriter

internal final class GpxFileWriter(private val gpxTrackWriter: IGpxTrackWriter, private val context: Context, private val userProfile: IUserProfile) : IGpxFileWriter {
    private val creatorName = context.getString(R.string.app_name)

    private val metadataNameTemplate: String = context.getString(R.string.gpx_metadata_name)
    private val metadataDescriptionTemplate: String = context.getString(R.string.gpx_metadata_description)

    public override fun writeGpxContent(trackRecordings: List<TrackRecording>, stream: OutputStream) {
        val streamWriter = OutputStreamWriter(stream, Charsets.UTF_8)

        val userName = this.userProfile.name

        val currentDateTime = DateTime.now()
        val metadataName = String.format(this.metadataNameTemplate, currentDateTime.formatDefault())
        val metadataDescription = String.format(this.metadataDescriptionTemplate, currentDateTime.formatDefault(), trackRecordings.count())

        streamWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>")
        streamWriter.write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"${this.creatorName}\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">")
        streamWriter.write("<metadata>")
        streamWriter.write("<name>${metadataName}</name>")
        streamWriter.write("<desc>${metadataDescription}</desc>")
        streamWriter.write("<author>")
        streamWriter.write("<name>${userName}</name>")
        streamWriter.write("</author>")
        streamWriter.write("</metadata>")

        for(trackRecording in trackRecordings) {
            this.gpxTrackWriter.writeGpxTrack(trackRecording, streamWriter)
        }

        streamWriter.write("</gpx>")

        streamWriter.flush()
    }
}