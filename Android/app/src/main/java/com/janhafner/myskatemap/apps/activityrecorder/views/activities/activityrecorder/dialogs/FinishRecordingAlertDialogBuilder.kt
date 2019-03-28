package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.janhafner.myskatemap.apps.activityrecorder.R

internal final class FinishRecordingAlertDialogBuilder(context: Context): AlertDialog.Builder(context) {
    init {
        this.setTitle(R.string.activityrecorderactivity_finish_confirmation_title)
        this.setCancelable(true)
        this.setMessage(R.string.activityrecorderactivity_finish_confirmation_message)
        this.setIcon(R.drawable.ic_help_24dp)
        this.setNegativeButton(R.string.activityrecorderactivity_finish_confirmation_button_no_label, null)
    }
}