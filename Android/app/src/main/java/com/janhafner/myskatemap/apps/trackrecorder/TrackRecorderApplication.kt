package com.janhafner.myskatemap.apps.trackrecorder

import android.app.Application
import android.os.StrictMode
import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule


internal final class TrackRecorderApplication: Application() {
    public val injector: ApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

    public override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        super.onCreate()
    }
}