package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import com.janhafner.myskatemap.apps.trackrecorder.statistics.ITrackRecordingStatistic
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession :  IDestroyable {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val burnedEnergyChanged: Observable<BurnedEnergy>

    val stateChanged: Observable<TrackRecordingSessionState>

    val recordingSaved: Observable<com.janhafner.myskatemap.apps.trackrecorder.Nothing>

    val sessionClosed: Observable<ITrackRecordingSession>

    val trackingStartedAt: DateTime

    val statistic: ITrackRecordingStatistic

    var name: String

    var comment: String?

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}