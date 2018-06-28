package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.content.Intent
import com.janhafner.myskatemap.apps.trackrecorder.checkAllAppPermissions
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivityPresenter(private val view: SplashscreenActivity) {
    init {
        JodaTimeAndroid.init(this.view)

        this.view.checkAllAppPermissions().subscribe { areAllGranted ->
            if(areAllGranted) {
                this.view.startActivity(Intent(this.view, TrackRecorderActivity::class.java))

                this.view.finish()
            } else {
                this.view.finishAndRemoveTask()
            }
        }
    }
}