package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

internal final class TrackRecorderTabsAdapter(tabDefinitions: List<TabDefinition>, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    private var availableTabCount: Int = 0

    private val availableTabDefinitions: Map<Int, TabDefinition>

    init {
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
}