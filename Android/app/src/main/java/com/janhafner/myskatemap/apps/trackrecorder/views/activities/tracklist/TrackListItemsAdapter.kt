package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.adapter.ArrayRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_track_list_item.view.*

internal final class TrackListItemsAdapter : ArrayRecyclerViewAdapter<TrackListItem>(R.layout.activity_track_list_item) {
    public override fun onItemViewCreated(itemView: View, item: TrackListItem, position: Int) {
        itemView.activity_track_list_item_displayname.text = item.displayName
        itemView.activity_track_list_item_distance.text = item.distance.toString()
        itemView.activity_track_list_item_recordingtime.text = item.recordingTime.toString()
    }
}