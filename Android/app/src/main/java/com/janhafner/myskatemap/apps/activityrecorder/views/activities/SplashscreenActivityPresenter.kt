package com.janhafner.myskatemap.apps.activityrecorder.views.activities

import android.content.Intent
import com.janhafner.myskatemap.apps.activityrecorder.checkAllAppPermissions
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.ActivityRecorderActivity
import net.danlew.android.joda.JodaTimeAndroid

internal final class SplashscreenActivityPresenter(val view: SplashscreenActivity) {
    init {
        JodaTimeAndroid.init(this.view)

        this.view.checkAllAppPermissions().subscribe { areAllGranted ->
            if(areAllGranted) {
                this.view.startActivity(Intent(this.view, ActivityRecorderActivity::class.java))

                this.view.finish()
            } else {
                this.view.finishAndRemoveTask()
            }
        }
    }
}