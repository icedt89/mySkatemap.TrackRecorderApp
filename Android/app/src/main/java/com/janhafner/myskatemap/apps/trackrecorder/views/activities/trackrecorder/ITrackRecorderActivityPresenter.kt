package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecorderActivityPresenter {
    val trackingStartedAtChanged: Observable<DateTime>

    val recordingTimeChanged: Observable<Period>

    val trackDistanceChanged: Observable<Float>

    val trackSessionStateChanged: Observable<TrackRecorderServiceState>

    val locationsChangedAvailable: Observable<Observable<Location>>

    val canStartResumeRecordingChanged: Observable<Boolean>

    val canPauseRecordingChanged: Observable<Boolean>

    val canDiscardRecordingChanged: Observable<Boolean>

    val canFinishRecordingChanged: Observable<Boolean>

    val attachmentsChanged: Observable<List<Attachment>>

    fun startAndBindService()

    fun unbindService()

    fun saveCurrentRecording()

    fun addAttachment(attachment: Attachment)

    fun removeAttachment(attachment: Attachment)

    fun startResumeRecording()

    fun pauseRecording()

    fun discardRecording()

    fun finishRecording()
}