package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityhistory

import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import org.joda.time.Period

internal final class ActivityHistoryItem(public val activityInfo: ActivityInfo) {
    public var displayName: String = this.activityInfo.displayName!!

    public var distance: Float = this.activityInfo.distance!!

    public var recordingTime: Period = this.activityInfo.recordingTime

    public var activityCode: String = this.activityInfo.activityCode!!
}