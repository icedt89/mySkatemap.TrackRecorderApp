package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface IDataTabFragmentPresenter {
    val trackingStartedAtChanged: Observable<DateTime>

    val recordingTimeChanged: Observable<Period>

    val trackDistanceChanged: Observable<Float>

    val trackSessionStateChanged: Observable<TrackRecorderServiceState>

    val locationsChangedAvailable: Observable<Observable<Location>>
}