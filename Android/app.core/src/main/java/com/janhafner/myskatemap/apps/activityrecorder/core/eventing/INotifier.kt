package com.janhafner.myskatemap.apps.activityrecorder.core.eventing

import io.reactivex.Observable

public interface INotifier {
    fun publish(event: Any)

    val notifications: Observable<Any>
}

