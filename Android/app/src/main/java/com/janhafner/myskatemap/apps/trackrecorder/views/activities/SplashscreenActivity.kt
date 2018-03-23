package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.start.StartActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivity: AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        JodaTimeAndroid.init(this)

        //this.startActivity(Intent(this, SettingsActivity::class.java), savedInstanceState)
        this.startActivity(Intent(this, StartActivity::class.java), savedInstanceState)

        this.finish()
    }
}