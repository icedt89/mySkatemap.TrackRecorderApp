package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        JodaTimeAndroid.init(this)

        val startTrackRecorderActivityIntent = Intent(this, TrackRecorderActivity::class.java)
        this.startActivity(startTrackRecorderActivityIntent, savedInstanceState)

        this.finish()
    }
}