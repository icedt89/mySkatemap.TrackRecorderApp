package com.janhafner.myskatemap.apps.trackrecorder.services

import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording

internal interface ITrackService : ICrudRepository<TrackRecording> {
    fun getAttachmentHandler(trackRecording: TrackRecording): IAttachmentHandler
}