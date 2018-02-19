package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments.AttachmentsTabFragment

internal final class TrackRecorderTabsAdapter(context: Context, fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {
    private val mapTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_map_title)

    private val dataTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_data_title)

    private val attachmentsTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_attachments_title)

    public override fun getCount(): Int {
        return 3
    }

    public override fun getPageTitle(position: Int): CharSequence {
        when(position) {
            0 ->
                return this.mapTabTitle
            1 ->
                return this.dataTabTitle
            2 ->
                return this.attachmentsTabTitle
        }

        throw IllegalArgumentException("position")
    }

    public override fun getItem(position: Int): Fragment {
        when(position) {
            0 ->
                return MapTabFragment()
            1 ->
                return DataTabFragment()
            2 ->
                return AttachmentsTabFragment()
          }

        throw IllegalArgumentException("position")
    }
}