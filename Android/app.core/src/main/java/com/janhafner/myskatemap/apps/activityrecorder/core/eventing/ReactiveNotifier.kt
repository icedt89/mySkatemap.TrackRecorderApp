package com.janhafner.myskatemap.apps.activityrecorder.core.eventing

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

public final class ReactiveNotifier: INotifier, IDestroyable {
    private val subject: Subject<Any> = PublishSubject.create()

    public override val notifications: Observable<Any>
        get() = this.subject

    public override fun publish(event: Any) {
        this.subject.onNext(event)
    }

    public override fun destroy() {
        this.subject.onComplete()
    }
}