package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.IAttachmentsTabFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.IDataTabFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.IMapTabFragmentPresenter
import io.reactivex.Observable

internal interface ITrackRecorderActivityPresenter: IMapTabFragmentPresenter, IDataTabFragmentPresenter, IAttachmentsTabFragmentPresenter {
    val canPauseRecordingChanged: Observable<Boolean>

    val canDiscardRecordingChanged: Observable<Boolean>

    val canFinishRecordingChanged: Observable<Boolean>

    fun startAndBindService()

    fun unbindService()

    fun saveCurrentRecording()

    fun discardRecording()

    fun finishRecording()
}