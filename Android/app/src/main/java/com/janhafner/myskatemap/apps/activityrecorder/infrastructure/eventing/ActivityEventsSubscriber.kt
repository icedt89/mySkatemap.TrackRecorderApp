package com.janhafner.myskatemap.apps.activityrecorder.infrastructure.eventing

import android.util.Log
import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.calculateDistance
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivityDeletedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivitySavedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.core.formatDefault
import com.janhafner.myskatemap.apps.activityrecorder.core.toLiteAndroidLocation
import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityQueryService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

internal final class ActivityEventsSubscriber(notifier: INotifier, activityQueryService: IActivityQueryService) : IDestroyable {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.subscriptions.addAll(
            notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(ActivitySavedEvent::class.java)
                .map {
                    val activityInfo = ActivityInfo()
                    activityInfo.displayName = it.activity.startedAt.formatDefault()
                    activityInfo.id = it.activity.id
                    activityInfo.recordingTime = it.activity.recordingTime
                    activityInfo.distance = it.activity.locations.map { it.toLiteAndroidLocation() }.calculateDistance()
                    activityInfo.startedAt = it.activity.startedAt
                    activityInfo.activityCode = it.activity.activityCode

                    activityInfo
                }
                .flatMapSingle {
                    activityQueryService.saveActivityInfo(it)
                }
                .doOnError {
                    // TODO: Implement error handling
                    Log.e("TRES", it.message)
                }
                .subscribe(),
            notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(ActivityDeletedEvent::class.java)
                .flatMapSingle {
                    activityQueryService.deleteActivityInfo(it.activityId)
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