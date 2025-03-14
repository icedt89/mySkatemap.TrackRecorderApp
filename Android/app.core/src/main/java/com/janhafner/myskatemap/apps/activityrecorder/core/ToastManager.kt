package com.janhafner.myskatemap.apps.activityrecorder.core

import android.content.Context
import android.widget.Toast

public final class ToastManager {
    public companion object {
        private var currentToast: Toast? = null

        public fun showToast(context: Context, text: String, duration: Int) {
            if (currentToast != null) {
                currentToast!!.cancel()
            }

            currentToast = Toast.makeText(context.applicationContext, text, duration)

            currentToast!!.show()
        }
    }
}