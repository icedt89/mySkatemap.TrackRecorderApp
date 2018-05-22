package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.content.Intent
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivityPresenter(private val splashscreenActivity: SplashscreenActivity) {
    init {
        JodaTimeAndroid.init(this.splashscreenActivity)

        this.splashscreenActivity.startActivity(Intent(this.splashscreenActivity, TrackListActivity::class.java))

        this.splashscreenActivity.finish()
    }
}