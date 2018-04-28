package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackviewer

import com.janhafner.myskatemap.apps.trackrecorder.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

internal interface ITrackViewerActivityPresenter {
    val trackingStartedAt: DateTime

    val recordingTime: Period

    val trackDistance: Float

    val name: String

    val comment: String

    val locations: List<Location>

    val attachments: List<Attachment>

    fun bindToActivity(trackViewerActivity: TrackViewerActivity)
}

internal final class TrackViewerActivityPresenter(private val trackService: ITrackService): ITrackViewerActivityPresenter {
    public override lateinit var trackingStartedAt: DateTime

    public override lateinit var recordingTime: Period

    public override var trackDistance: Float = 0.0f

    public override lateinit var name: String

    public override lateinit var comment: String

    public override lateinit var locations: List<Location>

    public override lateinit var attachments: List<Attachment>

    public override fun bindToActivity(trackViewerActivity: TrackViewerActivity) {
        val trackRecordingId = UUID.fromString(trackViewerActivity.intent.getStringExtra("ID"))

        val hasTrackRecording = this.trackService.hasTrackRecording(trackRecordingId)
        if(hasTrackRecording)
        {
            var trackRecording = this.trackService.getTrackRecording(trackRecordingId)

        }
        else {

        }
    }
}