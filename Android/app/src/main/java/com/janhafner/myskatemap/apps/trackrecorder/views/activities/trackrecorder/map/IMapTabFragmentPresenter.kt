package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.Observable

internal interface IMapTabFragmentPresenter {
    val trackSessionStateChanged: Observable<TrackRecorderServiceState>

    val locationsChangedAvailable: Observable<Observable<Location>>

    fun startResumeRecording()

    fun pauseRecording()

    val canStartResumeRecordingChanged: Observable<Boolean>
}

