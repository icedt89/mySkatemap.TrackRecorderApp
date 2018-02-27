package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface IMapTabFragmentPresenter {
    val trackSessionStateChanged: Observable<TrackRecorderServiceState>

    val locationsChangedAvailable: Observable<Observable<Location>>

    fun startResumeRecording()

    fun pauseRecording()

    val canStartResumeRecordingChanged: Observable<Boolean>
}

internal interface IDataTabFragmentPresenter {
    val trackingStartedAtChanged: Observable<DateTime>

    val recordingTimeChanged: Observable<Period>

    val trackDistanceChanged: Observable<Float>

    val trackSessionStateChanged: Observable<TrackRecorderServiceState>

    val locationsChangedAvailable: Observable<Observable<Location>>
}

internal interface IAttachmentsTabFragmentPresenter {
    val attachmentsChanged: Observable<List<Attachment>>

    val attachmentsSelected: Observable<List<Attachment>>

    fun addAttachment(attachment: Attachment)

    fun removeAttachment(attachment: Attachment)

    fun setSelectedAttachments(attachments: List<Attachment>)
}