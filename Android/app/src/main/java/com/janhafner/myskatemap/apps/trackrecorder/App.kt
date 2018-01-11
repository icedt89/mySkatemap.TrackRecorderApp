package com.janhafner.myskatemap.apps.trackrecorder

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

internal final class App : Application {
    public constructor(): super();

    override fun onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }
}