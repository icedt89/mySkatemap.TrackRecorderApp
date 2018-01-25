package com.janhafner.myskatemap.apps.trackrecorder.location

import android.os.Binder
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording

internal final class TrackRecorderServiceBinder(private val trackRecorderService : ITrackRecorderService) : Binder(), ITrackRecorderService {
    public override val currentSession: ITrackRecordingSession?
        get() = this.trackRecorderService.currentSession

    public override fun discardTracking() {
        this.trackRecorderService.discardTracking()
    }

    public override fun finishTracking(): TrackRecording {
        return this.trackRecorderService.finishTracking()
    }

    public override fun createSession(trackRecording: TrackRecording): ITrackRecordingSession {
        return this.trackRecorderService.createSession(trackRecording)
    }

    public override fun createSession(name: String): ITrackRecordingSession {
        return this.trackRecorderService.createSession(name)
    }
}