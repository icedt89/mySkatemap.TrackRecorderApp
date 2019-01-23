package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.eventing

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.calculateDistance
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.TrackRecordingDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.TrackRecordingSavedEvent
import com.janhafner.myskatemap.apps.trackrecorder.common.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.common.toLiteAndroidLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
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

                    trackInfo
                }
                .flatMapSingle {
                    // TODO: Implement error handling
                    trackQueryService.saveTrackInfo(it)
                }
                .retry(2)
                .subscribe(),
            notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(TrackRecordingDeletedEvent::class.java)
                .flatMapSingle {
                    // TODO: Implement error handling
                    trackQueryService.deleteTrackInfo(it.trackRecordingId)
                }
                .retry(2)
                .subscribe()
        )
    }

    public override fun destroy() {
        this.subscriptions.dispose()
    }
}