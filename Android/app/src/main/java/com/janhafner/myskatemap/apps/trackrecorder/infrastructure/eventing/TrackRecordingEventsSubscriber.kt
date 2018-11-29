package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.eventing

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.TrackRecordingSavedEvent
import com.janhafner.myskatemap.apps.trackrecorder.common.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import io.reactivex.disposables.CompositeDisposable

internal final class TrackRecordingEventsSubscriber(notifier: INotifier, trackQueryService: ITrackQueryService, distanceCalculator: IDistanceCalculator) : IDestroyable {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.subscriptions.addAll(
            notifier.notifications
                .ofType(TrackRecordingSavedEvent::class.java)
                .map {
                    it.trackRecording
                }
                .subscribe {
                    val trackInfo = TrackInfo()
                    trackInfo.displayName = it.startedAt.formatDefault()
                    trackInfo.id = it.id
                    trackInfo.recordingTime = it.recordingTime
                    trackInfo.distance = distanceCalculator.calculateDistance(it.locations)

                    // TODO: Implement error handling
                    trackQueryService.saveTrackInfo(trackInfo).subscribe()
                }
        )
    }

    public override fun destroy() {
        this.subscriptions.dispose()
    }
}