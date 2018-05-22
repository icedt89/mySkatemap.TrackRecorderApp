package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.os.Binder
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording

internal final class TrackRecorderServiceBinder(private val trackRecorderService: ITrackRecorderService): Binder(), ITrackRecorderService {
    public override val currentSession: ITrackRecordingSession?
        get() = this.trackRecorderService.currentSession

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        return this.trackRecorderService.useTrackRecording(trackRecording)
    }
}