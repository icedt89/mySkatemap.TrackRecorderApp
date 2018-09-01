package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatTimeOnlyDefault
import com.janhafner.myskatemap.apps.trackrecorder.views.ArrayRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_track_recorder_tab_debug_log_output_item.view.*

internal final class LogOutputItemsAdapter : ArrayRecyclerViewAdapter<LogItem>(R.layout.activity_track_recorder_tab_debug_log_output_item) {
    protected override fun onItemViewCreated(itemView: View, item: LogItem, position: Int) {
        itemView.log_output_message.text = item.toString()
    }
}