package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackviewer

import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import org.joda.time.DateTime
import org.joda.time.Period

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
        val trackRecordingId = trackViewerActivity.intent.getStringExtra("ID")

        val trackRecording = this.trackService.getTrackRecording(trackRecordingId)
        if(trackRecording != null)
        {

        } else {

        }
    }
}