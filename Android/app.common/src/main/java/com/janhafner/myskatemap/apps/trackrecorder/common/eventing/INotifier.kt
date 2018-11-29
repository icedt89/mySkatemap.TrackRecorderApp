package com.janhafner.myskatemap.apps.trackrecorder.common.eventing

import io.reactivex.Observable

public interface INotifier {
    fun publish(event: Any)

    val notifications: Observable<Any>
}

