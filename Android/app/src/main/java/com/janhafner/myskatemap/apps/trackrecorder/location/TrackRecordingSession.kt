package com.janhafner.myskatemap.apps.trackrecorder.location

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TrackRecordingSession(public override val trackDistanceChanged: Observable<Float>,
                                           public override val recordingTimeChanged: Observable<Period>,
                                           public override val locationsChanged: Observable<Location>,
                                           public override val stateChanged: Observable<TrackRecorderServiceState>,
                                           private val trackRecorderService: TrackRecorderService,
                                           trackingStartedAt: DateTime): ITrackRecordingSession {
    public override var trackingStartedAt: DateTime = trackingStartedAt
        private set

    public override fun resumeTracking() {
        this.trackRecorderService.resumeTracking()
    }

    public override fun pauseTracking() {
        this.trackRecorderService.pauseTracking()
    }

    public override fun saveTracking() {
        this.trackRecorderService.saveTracking()
    }
}