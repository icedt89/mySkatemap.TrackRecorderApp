package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import androidx.fragment.app.Fragment

internal abstract class DashboardTileFragment : Fragment() {
    public var presenter: DashboardTileFragmentPresenter? = null
        public set(value) {
            field?.destroy()

            if(value != null) {
                value.initialize(this)
            }

            field = value
        }

    public override fun onDestroy() {
        this.presenter = null

        super.onDestroy()
    }
}