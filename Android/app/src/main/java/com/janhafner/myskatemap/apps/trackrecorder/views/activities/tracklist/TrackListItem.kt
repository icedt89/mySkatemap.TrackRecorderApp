package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import org.joda.time.Period

internal final class TrackListItem {
    public var displayName: String? = null

    public var distance: Float? = null

    public var recordingTime: Period? = null

    public var isDeletable: Boolean = false
}