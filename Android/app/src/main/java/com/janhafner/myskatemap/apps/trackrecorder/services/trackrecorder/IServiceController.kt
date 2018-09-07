package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal interface IServiceController<TBinder: IBinder> {
    val isClientBoundChanged: Observable<Boolean>

    val isClientBound: Boolean

    val currentBinder: TBinder?

    fun startAndBindService() : Disposable
}