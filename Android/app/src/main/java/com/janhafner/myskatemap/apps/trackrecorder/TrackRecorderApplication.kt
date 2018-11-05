package com.janhafner.myskatemap.apps.trackrecorder

import android.accounts.AccountManager
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
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }

        super.onCreate()
    }
}