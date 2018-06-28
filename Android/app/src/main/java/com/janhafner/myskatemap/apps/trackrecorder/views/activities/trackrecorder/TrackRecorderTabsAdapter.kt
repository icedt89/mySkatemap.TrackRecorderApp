package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data.DataTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment

internal final class TrackRecorderTabsAdapter(context: Context, fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {
    private val dashboardTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_dashboard_title)

    private val mapTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_map_title)

    private val dataTabTitle: String = context.getString(R.string.trackrecorderactivity_tab_data_title)

    public override fun getCount(): Int {
        return 3
    }

    public override fun getPageTitle(position: Int): CharSequence {
        when(position) {
            0 ->
                return this.dashboardTabTitle
            1 ->
                return this.mapTabTitle
            2 ->
                return this.dataTabTitle
        }

        throw IllegalArgumentException("position")
    }

    public override fun getItem(position: Int): Fragment {
        when(position) {
            0 ->
                return DashboardTabFragment()
            1 ->
                return MapTabFragment()
            2 ->
                return DataTabFragment()
          }

        throw IllegalArgumentException("position")
    }
}