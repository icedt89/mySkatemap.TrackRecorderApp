package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.Nothing
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TrackRecordingSession(public override val trackDistanceChanged: Observable<Float>,
                                           public override val recordingTimeChanged: Observable<Period>,
                                           public override val locationsChanged: Observable<Location>,
                                           public override val stateChanged: Observable<TrackRecorderServiceState>,
                                           public override val recordingSaved: Observable<Nothing>,
                                           private val trackRecorderService: TrackRecorderService): ITrackRecordingSession {
    public override val trackingStartedAt: DateTime
        get() = this.trackRecorderService.currentTrackRecording!!.trackingStartedAt

    public override var name: String
        get() = this.trackRecorderService.currentTrackRecording!!.name
        set(value) {
            this.trackRecorderService.currentTrackRecording!!.name = value
        }

    public override var comment: String?
        get() = this.trackRecorderService.currentTrackRecording!!.comment
        set(value) {
            this.trackRecorderService.currentTrackRecording!!.comment = value
        }

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession>
        get() = this.sessionClosedSubject

    public override fun resumeTracking() {
        this.trackRecorderService.resumeTracking()
    }

    public override fun pauseTracking() {
        this.trackRecorderService.pauseTracking()
    }

    public override fun saveTracking() {
        this.trackRecorderService.saveTracking()
    }

    public override fun discardTracking() {
        this.trackRecorderService.discardTracking()
    }

    public override fun finishTracking(): TrackRecording {
        return this.trackRecorderService.finishTracking()
    }

    public override fun destroy() {
        throw NotImplementedError()
    }
}