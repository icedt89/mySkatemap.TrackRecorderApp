package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackInfo
import org.joda.time.Period

internal final class TrackListItem(public val trackInfo: TrackInfo) {
    public var displayName: String = this.trackInfo.displayName!!

    public var distance: Float = this.trackInfo.distance!!

    public var recordingTime: Period = this.trackInfo.recordingTime

    public var activityCode: String = this.trackInfo.activityCode!!
}