package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentFactory
import javax.inject.Inject

internal final class DashboardTabFragment : Fragment() {
    private var presenter: DashboardTabFragmentPresenter? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var dashboardTileFragmentFactory: IDashboardTileFragmentFactory

    @Inject
    public lateinit var dashboardService: ICrudRepository<Dashboard>

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DashboardTabFragmentPresenter(this, this.appSettings, this.dashboardService, this.dashboardTileFragmentFactory)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(this.presenter != null) {
            this.presenter!!.setUserVisibleHint(isVisibleToUser)
        }
    }
}

