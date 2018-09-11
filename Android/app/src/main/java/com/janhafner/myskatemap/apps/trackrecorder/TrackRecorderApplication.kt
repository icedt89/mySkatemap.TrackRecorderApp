package com.janhafner.myskatemap.apps.trackrecorder

import android.app.Application
import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule

internal final class TrackRecorderApplication: Application() {
    public val injector: ApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
}