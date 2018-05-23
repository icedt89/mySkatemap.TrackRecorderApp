package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.content.Intent
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivityPresenter(private val view: SplashscreenActivity) {
    init {
        JodaTimeAndroid.init(this.view)

        this.view.startActivity(Intent(this.view, TrackListActivity::class.java))

        this.view.finish()
    }
}