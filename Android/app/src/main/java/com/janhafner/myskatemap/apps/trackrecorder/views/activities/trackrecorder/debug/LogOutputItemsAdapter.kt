package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.common.Counted
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.ArrayRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_track_recorder_tab_debug_log_output_item.view.*

internal final class LogOutputItemsAdapter : ArrayRecyclerViewAdapter<Counted<LogItem>>(R.layout.activity_track_recorder_tab_debug_log_output_item) {
    protected override fun onItemViewCreated(itemView: View, item: Counted<LogItem>, position: Int) {
        itemView.log_output_message.text = "#${item.count} ${item.value}"
    }
}