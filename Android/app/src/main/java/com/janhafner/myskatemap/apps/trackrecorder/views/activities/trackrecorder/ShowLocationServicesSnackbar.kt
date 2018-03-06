package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.app.Activity
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.startLocationSourceSettingsActivity

internal final class ShowLocationServicesSnackbar {
    companion object {
        public fun make(activity: Activity, view: View): Snackbar {
            return Snackbar.make(view, activity.getString(R.string.trackrecorderactivity_snackbar_locationservices_message), Snackbar.LENGTH_LONG)
                .setAction(activity.getString(R.string.trackrecorderactivity_snackbar_locationservices_action_enable), {
                    activity.startLocationSourceSettingsActivity()
                })
                .setActionTextColor(Color.YELLOW)
        }
    }
}