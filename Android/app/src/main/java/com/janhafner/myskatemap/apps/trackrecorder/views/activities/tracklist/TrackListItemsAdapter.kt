package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.views.ObservableArrayAdapter

internal final class TrackListItemsAdapter(context: Context): ObservableArrayAdapter<TrackRecording>(context, R.layout.activity_track_list_item) {
}