package com.janhafner.myskatemap.apps.trackrecorder

import android.app.Application
import android.os.StrictMode
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.eventing.TrackRecordingEventsSubscriber
import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject


internal final class TrackRecorderApplication: Application() {
    public val injector: ApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

    @Inject
    public lateinit var trackRecordingEventsSubscriber: TrackRecordingEventsSubscriber

    public override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        LeakCanary.install(this)

        val forcePreventStrictMode = true
        if (!forcePreventStrictMode && BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        this.injector.inject(this)
    }
}