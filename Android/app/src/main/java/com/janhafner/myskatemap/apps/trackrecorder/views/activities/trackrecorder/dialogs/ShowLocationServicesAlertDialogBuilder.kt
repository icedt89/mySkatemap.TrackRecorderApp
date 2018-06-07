package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class ShowLocationServicesAlertDialogBuilder(context: Context): AlertDialog.Builder(context) {
    init {
        this.setTitle(R.string.trackrecorderactivity_show_location_services_confirmation_title)
        this.setMessage(R.string.trackrecorderactivity_show_location_services_confirmation_message)
        this.setIcon(R.drawable.ic_dialog_warning)
    }
}