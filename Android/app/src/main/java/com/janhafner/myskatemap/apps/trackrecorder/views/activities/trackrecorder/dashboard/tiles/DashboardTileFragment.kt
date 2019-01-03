package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import androidx.fragment.app.Fragment

internal abstract class DashboardTileFragment : Fragment() {
    public var presenter: DashboardTileFragmentPresenter? = null
        public set(value) {
            field?.destroy()

            if(value != null) {
                value.onResume(this)

                field = value
            }
        }

    public override fun onDestroyView() {
        this.presenter?.destroy()

        super.onDestroyView()
    }

    public override fun onResume() {
        super.onResume()

        this.presenter?.onResume(this)
    }

    public override fun onPause() {
        super.onPause()

        this.presenter?.onPause()
    }
}