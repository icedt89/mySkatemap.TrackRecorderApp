package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.os.Binder
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable

internal final class TrackRecorderServiceBinder(private val trackRecorderService: ITrackRecorderService): Binder(), ITrackRecorderService {
    public override val hasCurrentSessionChanged: Observable<Boolean>
        get() = this.trackRecorderService.hasCurrentSessionChanged

    public override val currentSession: ITrackRecordingSession?
        get() = this.trackRecorderService.currentSession

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        return this.trackRecorderService.useTrackRecording(trackRecording)
    }
}