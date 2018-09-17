package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment

internal final class TrackRecorderTabsAdapter(context: Context, fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {
    private var availableTabCount: Int = 0

    private val availableTabDefinitions: Map<Int, TabDefinition>

    init {
        // TODO: Supply via constructor parameter
        val tabDefinitions: List<TabDefinition> = listOf(
                TabDefinition(context.getString(R.string.trackrecorderactivity_tab_dashboard_title), {
                    DashboardTabFragment()
                }, 0),
                TabDefinition(context.getString(R.string.trackrecorderactivity_tab_map_title), {
                    MapTabFragment()
                }, 1)
        )

        this.availableTabDefinitions = tabDefinitions.filter {
            it.isAvailable
        }.mapIndexed { index, tabDefinition -> index to tabDefinition }.toMap()
        this.availableTabCount = this.availableTabDefinitions.count()
    }

    public override fun getCount(): Int {
        return this.availableTabCount
    }

    public override fun getPageTitle(position: Int): CharSequence {
        val result = this.availableTabDefinitions.get(position)
        if(result != null) {
            return result.pageTitle
        }

        throw IllegalArgumentException("position")
    }

    public override fun getItem(position: Int): Fragment {
        val result = this.availableTabDefinitions.get(position)
        if(result != null) {
            return result.tabFragmentFactory()
        }

        throw IllegalArgumentException("position")
    }

    private final class TabDefinition(public val pageTitle: String, public val tabFragmentFactory: () -> Fragment, public val position: Int, public val isAvailable: Boolean = true) {
    }
}