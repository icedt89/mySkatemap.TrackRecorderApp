package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class DiscardRecordingAlertDialogBuilder(context: Context): AlertDialog.Builder(context) {
    init {
        this.setTitle(R.string.trackrecorderactivity_discard_confirmation_title)
        this.setCancelable(true)
        this.setMessage(R.string.trackrecorderactivity_discard_confirmation_message)
         this.setIcon(R.drawable.ic_warning_dark_24dp)
        this.setNegativeButton(R.string.trackrecorderactivity_discard_confirmation_button_no_label, null)
    }
}