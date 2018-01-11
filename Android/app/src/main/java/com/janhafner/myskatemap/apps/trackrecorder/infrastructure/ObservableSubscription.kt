package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import java.util.*

internal final class ObservableSubscription {
    private final var observable: Observable?;

    private final var observer: Observer?;

    public constructor(observable: Observable, observer: Observer) {
        if(observable == null) {
            throw IllegalArgumentException("observable");
        }

        if(observer == null) {
            throw IllegalArgumentException("observer");
        }

        this.observable = observable;
        this.observer = observer;
    }

    public final fun remove() {
        if(this.observable == null || this.observer == null) {
            return;
        }

        this.observable!!.deleteObserver(this.observer);

        this.observer = null;
        this.observable = null;
    }
}