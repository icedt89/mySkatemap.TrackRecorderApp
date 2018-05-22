package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.statistics.TrackRecordingStatistic
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TrackRecordingSession(public override val trackDistanceChanged: Observable<Float>,
                                           public override val recordingTimeChanged: Observable<Period>,
                                           public override val locationsChanged: Observable<Location>,
                                           public override val stateChanged: Observable<TrackRecorderServiceState>,
                                           public override val recordingSaved: Observable<ITrackRecorderService>,
                                           private val trackRecorderService: TrackRecorderService): ITrackRecordingSession {
    public override val trackingStartedAt: DateTime
        get() = this.trackRecorderService.currentTrackRecording!!.trackingStartedAt

    public override val statistic: TrackRecordingStatistic
        get() = this.trackRecorderService.currentTrackRecordingStatistic!!

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
}