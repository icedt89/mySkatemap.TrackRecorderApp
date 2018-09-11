package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal final class ServiceController<TService, TBinder: IBinder>(private val context: Context,
                                                                   private val serviceClass: Class<TService>)
        : IServiceController<TBinder> {
    private val isClientBoundChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isClientBoundChanged: Observable<Boolean> = this.isClientBoundChangedSubject.subscribeOn(Schedulers.computation())

    public override val isClientBound: Boolean
        get() = this.isClientBoundChangedSubject.value!!

    public override var currentBinder: TBinder? = null
        private set

    private var serviceControllerSubscription: Disposable? = null

    public override fun startAndBindService() : Disposable {
        if(this.serviceControllerSubscription != null) {
            return this.serviceControllerSubscription!!
        }

        // Starting from api level 26 a background services must be marked as "foreground"-service in order to get not killed by the OS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.context.startForegroundService(Intent(this.context, this.serviceClass))
        } else {
            this.context.startService(Intent(this.context, this.serviceClass))
        }

        val serviceControllerSubscription = ServiceControllerSubscription(this)

        this.context.bindService(Intent(this.context, this.serviceClass), serviceControllerSubscription, AppCompatActivity.BIND_AUTO_CREATE)

        return this.serviceControllerSubscription!!
    }

    private final class ServiceControllerSubscription<TService, TBinder: IBinder>(
            private val serviceController: ServiceController<TService, TBinder>) : ServiceConnection, Disposable {
        init {
            this.serviceController.serviceControllerSubscription = this
        }

        public override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            @Suppress("UNCHECKED_CAST")
            this.serviceController.currentBinder = service!! as TBinder

            this.serviceController.isClientBoundChangedSubject.onNext(true)
        }

        public override fun onServiceDisconnected(name: ComponentName?) {
            this.dispose()
        }

        private fun unbindClient() {
            this.serviceController.currentBinder = null

            this.serviceController.isClientBoundChangedSubject.onNext(false)
        }

        private var isDisposed: Boolean = false
        public override fun isDisposed(): Boolean {
            return this.isDisposed
        }

        public override fun dispose() {
            if(this.isDisposed) {
                return
            }

            try {
                this.serviceController.context.unbindService(this)
            } catch (exception: Exception) {
                Log.wtf("ServiceController", "WTF?!")
            }

            this.unbindClient()

            this.serviceController.serviceControllerSubscription = null

            this.isDisposed = true
        }
    }
}