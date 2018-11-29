package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.ReactiveNotifier
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.eventing.TrackRecordingEventsSubscriber
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class EventingModule {
    @Provides
    @Singleton
    public fun provideTrackRecordingEventsSubscriber(notifier: INotifier, trackQueryService: ITrackQueryService, distanceCalculator: IDistanceCalculator): TrackRecordingEventsSubscriber {
        return TrackRecordingEventsSubscriber(notifier, trackQueryService, distanceCalculator)
    }

    @Provides
    @Singleton
    public fun provideNotifier(): INotifier {
        return ReactiveNotifier()
    }
}