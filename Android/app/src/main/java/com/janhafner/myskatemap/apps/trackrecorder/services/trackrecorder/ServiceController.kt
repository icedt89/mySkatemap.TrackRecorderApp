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

internal final class ServiceController<TService, TBinder: IBinder>(private val context: Context,
                                                                   private val serviceClass: Class<TService>,
                                                                   completeOnDisconnect: Boolean = false)
        : IServiceController<TBinder> {
    private val serviceConnection = object : ServiceConnection {
        public override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            @Suppress("UNCHECKED_CAST")
            this@ServiceController.currentBinder = service!! as TBinder

            this@ServiceController.isClientBoundChangedSubject.onNext(true)
        }

        public override fun onServiceDisconnected(name: ComponentName?) {
            this@ServiceController.currentBinder = null

            this@ServiceController.isClientBoundChangedSubject.onNext(false)

            if(completeOnDisconnect){
                this@ServiceController.isClientBoundChangedSubject.onComplete()
            }
        }

        public override fun onBindingDied(name: ComponentName?) {
            this@ServiceController.currentBinder = null

            this@ServiceController.isClientBoundChangedSubject.onComplete()
        }
    }

    private val isClientBoundChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isClientBoundChanged: Observable<Boolean> = this.isClientBoundChangedSubject

    public override val isClientBound
        get() = this.isClientBoundChangedSubject.value

    public override var currentBinder: TBinder? = null
        private set

    public override fun startAndBindService() : Observable<Boolean> {
        if (this.currentBinder == null) {
            // Starting from api level 26 a background services must be marked as "foreground"-service in order to get not killed by the OS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.context.startForegroundService(Intent(this.context, this.serviceClass))
            } else {
                this.context.startService(Intent(this.context, this.serviceClass))
            }

            this.context.bindService(Intent(this.context, this.serviceClass), this.serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
        } else if(!this.isClientBoundChangedSubject.value) {
            this.isClientBoundChangedSubject.onNext(true)
        }

        return this.isClientBoundChanged
    }

    public override fun unbindService() {
        if (this.isClientBound) {
            this.context.unbindService(this.serviceConnection)
        }

        this.isClientBoundChangedSubject.onNext(false)
    }
}