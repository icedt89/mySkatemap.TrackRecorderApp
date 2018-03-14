package com.janhafner.myskatemap.apps.trackrecorder

import android.app.Application

internal final class TrackRecorderApplication: Application() {
    public val injector: ApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
}