package com.janhafner.myskatemap.apps.activityrecorder

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.eventing.ActivityEventsSubscriber
import com.janhafner.myskatemap.apps.activityrecorder.modules.ApplicationModule
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject


internal final class TrackRecorderApplication: Application() {
    public val injector: ApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

    @Inject
    public lateinit var activityEventsSubscriber: ActivityEventsSubscriber

    public override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }

            LeakCanary.install(this)

            Log.d("TrackRecorderApp", "LeakCanary installed!")

            val forcePreventStrictMode = true
            if (!forcePreventStrictMode) {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build())
                StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build())
            }
        }

        this.injector.inject(this)
    }
}