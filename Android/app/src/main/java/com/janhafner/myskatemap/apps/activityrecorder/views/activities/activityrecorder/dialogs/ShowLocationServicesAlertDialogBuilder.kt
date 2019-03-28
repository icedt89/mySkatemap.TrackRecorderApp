package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.janhafner.myskatemap.apps.activityrecorder.R

internal final class ShowLocationServicesAlertDialogBuilder(context: Context): AlertDialog.Builder(context) {
    init {
        this.setTitle(R.string.activityrecorderactivity_show_location_services_confirmation_title)
        this.setMessage(R.string.activityrecorderactivity_show_location_services_confirmation_message)
        this.setIcon(R.drawable.ic_warning_24dp)
    }
}