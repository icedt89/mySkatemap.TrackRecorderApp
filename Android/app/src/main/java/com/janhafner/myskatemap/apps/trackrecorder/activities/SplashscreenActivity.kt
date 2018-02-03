package com.janhafner.myskatemap.apps.trackrecorder.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder.TrackRecorderActivity

internal final class SplashscreenActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startTrackRecorderActivityIntent = Intent(this, TrackRecorderActivity::class.java)
        this.startActivity(startTrackRecorderActivityIntent, savedInstanceState)

        this.finish()
    }
}