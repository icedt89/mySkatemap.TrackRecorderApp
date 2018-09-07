package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.statistics.ITrackRecordingStatistic
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession :  IDestroyable {
    val distanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val burnedEnergyChanged: Observable<BurnedEnergy>

    val stateChanged: Observable<TrackRecordingSessionState>

    val sessionClosed: Observable<ITrackRecordingSession>

    val trackingStartedAt: DateTime

    val statistic: ITrackRecordingStatistic

    var name: String

    var comment: String?

    fun resumeTracking()

    fun pauseTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}