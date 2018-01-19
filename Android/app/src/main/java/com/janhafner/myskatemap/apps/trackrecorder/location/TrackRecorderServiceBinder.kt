package com.janhafner.myskatemap.apps.trackrecorder.location

import android.os.Binder
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import io.reactivex.Observable
import org.joda.time.Period

internal final class TrackRecorderServiceBinder(private val trackRecorderService : ITrackRecorderService) : Binder(), ITrackRecorderService {
    public override val locations: Observable<Location>
        get() = this.trackRecorderService.locations

    public override val stateChanged: Observable<TrackRecorderServiceState>
        get() = this.trackRecorderService.stateChanged

    public override val state: TrackRecorderServiceState
        get() = this.trackRecorderService.state

    public override val recordingDuration: Observable<Period>
        get() = this.trackRecorderService.recordingDuration

    public override val trackLength: Observable<Float>
        get() = this.trackRecorderService.trackLength

    public override fun resumeTracking() {
        this.trackRecorderService.resumeTracking()
    }

    public override fun finishTracking() : TrackRecording{
        return this.trackRecorderService.finishTracking()
    }

    public override fun useTrackRecording(trackRecording: TrackRecording) {
        this.trackRecorderService.useTrackRecording(trackRecording)
    }

    public override fun pauseTracking() {
        this.trackRecorderService.pauseTracking()
    }

    public override fun useTrackRecording(name: String) {
        this.trackRecorderService.useTrackRecording(name)
    }

    public override fun discardTracking() {
        this.trackRecorderService.discardTracking()
    }

    public override fun saveTracking() {
        this.trackRecorderService.saveTracking()
    }
}