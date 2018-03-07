package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.os.Binder
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import io.reactivex.Observable

internal final class TrackRecorderServiceBinder(private val trackRecorderService: ITrackRecorderService): Binder(), ITrackRecorderService {
    public override val locationServicesAvailability: Observable<Boolean>
        get() = this.trackRecorderService.locationServicesAvailability

    public override val currentSession: ITrackRecordingSession?
        get() = this.trackRecorderService.currentSession

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        return this.trackRecorderService.useTrackRecording(trackRecording)
    }
}