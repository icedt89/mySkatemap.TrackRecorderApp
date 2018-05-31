package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class ServiceController<TService, TBinder: IBinder>(private val context: Context, private val serviceClass: Class<TService>, completeOnDisconnect: Boolean = false) {
    private val serviceConnection = object : ServiceConnection {
        public override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            @Suppress("UNCHECKED_CAST")
            this@ServiceController.currentBinder = service!! as TBinder

            this@ServiceController.isBoundChangedSubject.onNext(true)
        }

        public override fun onServiceDisconnected(name: ComponentName?) {
            this@ServiceController.currentBinder = null

            this@ServiceController.isBoundChangedSubject.onNext(false)

            if(completeOnDisconnect){
                this@ServiceController.isBoundChangedSubject.onComplete()
            }
        }

        public override fun onBindingDied(name: ComponentName?) {
            this@ServiceController.currentBinder = null

            this@ServiceController.isBoundChangedSubject.onComplete()
        }
    }

    private val isBoundChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public val isBoundChanged: Observable<Boolean> = this.isBoundChangedSubject

    public val isBound
        get() = this.isBoundChangedSubject.value

    public var currentBinder: TBinder? = null
        private set

    public fun startAndBindService() : Observable<Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.context.startForegroundService(Intent(this.context, this.serviceClass))
        } else {
            this.context.startService(Intent(this.context, this.serviceClass))
        }

        this.context.bindService(Intent(this.context, this.serviceClass), this.serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)

        return this.isBoundChanged
    }

    public fun unbindService() {
        this.context.unbindService(this.serviceConnection)
    }
}