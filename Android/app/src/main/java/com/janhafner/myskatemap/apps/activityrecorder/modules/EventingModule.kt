package com.janhafner.myskatemap.apps.activityrecorder.modules

import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ReactiveNotifier
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.eventing.ActivityEventsSubscriber
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityQueryService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class EventingModule {
    @Provides
    @Singleton
    public fun provideActivityEventsSubscriber(notifier: INotifier, activityQueryService: IActivityQueryService): ActivityEventsSubscriber {
        return ActivityEventsSubscriber(notifier, activityQueryService)
    }

    @Provides
    @Singleton
    public fun provideNotifier(): INotifier {
        return ReactiveNotifier()
    }
}