package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import com.janhafner.myskatemap.apps.trackrecorder.formatTimeOnlyDefault
import org.joda.time.DateTime

internal final class LogItem(public val loggedAt: DateTime, public val message: String) {
    public override fun toString(): String {
        return "${this.loggedAt.formatTimeOnlyDefault()}: ${this.message}"
    }
}