package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.startLocationSourceSettingsActivity

internal final class ShowLocationServicesSnackbar {
    companion object {
        public fun make(context: Context, view: View): Snackbar {
            return Snackbar.make(view, context.getString(R.string.trackrecorderactivity_snackbar_locationservices_message), Snackbar.LENGTH_LONG)
                .setAction(context.getString(R.string.trackrecorderactivity_snackbar_locationservices_action_enable), {
                    context.startLocationSourceSettingsActivity()
                })
                .setActionTextColor(Color.YELLOW)
        }
    }
}