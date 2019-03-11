package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.eventing

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.core.calculateDistance
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackRecordingDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackRecordingSavedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.core.toLiteAndroidLocation
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

internal final class TrackRecordingEventsSubscriber(notifier: INotifier, trackQueryService: ITrackQueryService) : IDestroyable {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.subscriptions.addAll(
            notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(TrackRecordingSavedEvent::class.java)
                .map {
                    val trackInfo = TrackInfo()
                    trackInfo.displayName = it.trackRecording.startedAt.formatDefault()
                    trackInfo.id = it.trackRecording.id
                    trackInfo.recordingTime = it.trackRecording.recordingTime
                    trackInfo.distance = it.trackRecording.locations.map { it.toLiteAndroidLocation() }.calculateDistance()
                    trackInfo.startedAt = it.trackRecording.startedAt
                    trackInfo.activityCode = it.trackRecording.activityCode

                    trackInfo
                }
                .flatMapSingle {
                    trackQueryService.saveTrackInfo(it)
                }
                .doOnError {
                    // TODO: Implement error handling
                    Log.e("TRES", it.message)
                }
                .subscribe(),
            notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(TrackRecordingDeletedEvent::class.java)
                .flatMapSingle {
                    trackQueryService.deleteTrackInfo(it.trackRecordingId)
                }
                .doOnError {
                    // TODO: Implement error handling
                    Log.e("TRES", it.message)
                }
                .subscribe()
        )
    }

    public override fun destroy() {
        this.subscriptions.dispose()
    }
}