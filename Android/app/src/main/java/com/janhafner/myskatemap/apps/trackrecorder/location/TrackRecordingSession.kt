package com.janhafner.myskatemap.apps.trackrecorder.location

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.Period

internal final class TrackRecordingSession(public override val trackDistanceChanged : Observable<Float>,
                                           public override val recordingTimeChanged : Observable<Period>,
                                           public override val locationsChanged : Observable<Location>,
                                           public override val stateChanged : Observable<TrackRecorderServiceState>,
                                           private val trackRecorderService : TrackRecorderService) : ITrackRecordingSession {
    private val subscriptions : CompositeDisposable = CompositeDisposable()

    init {
        this.subscriptions.addAll(
                this.trackDistanceChanged.subscribe {
                currentDistance ->
                    this.trackDistance = currentDistance
            },
                this.recordingTimeChanged.subscribe {
                currentRecordingTime ->
                    this.recordingTime = currentRecordingTime
            },
                this.stateChanged.subscribe {
                currentState ->
                    this.state = currentState
            }
        )
    }

    public override var trackDistance : Float = 0f
        private set

    public override var recordingTime : Period = Period.ZERO
        private set

    public override var state : TrackRecorderServiceState = TrackRecorderServiceState.Initializing
        private set

    public override fun terminate() {
        this.subscriptions.clear()
        this.subscriptions.dispose()
    }

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