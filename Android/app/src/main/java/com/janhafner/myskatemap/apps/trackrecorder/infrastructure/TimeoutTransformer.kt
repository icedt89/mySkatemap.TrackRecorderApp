package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.common.IObservableTimeout
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

public final class TimeoutTransformer<TStream>(private val observableTimeout: IObservableTimeout, private val mapper: (onTimeout: Unit) -> TStream): ObservableTransformer<TStream, TStream> {
    public override fun apply(upstream: Observable<TStream>): ObservableSource<TStream> {
        return upstream
                .doOnNext{
                    this.observableTimeout.restart()
                }
                .doOnComplete {
                    this.observableTimeout.destroy()
                }
                .mergeWith(this.observableTimeout
                        .timedOut
                        .map(mapper))
    }
}