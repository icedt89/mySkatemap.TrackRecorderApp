package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.IDashboardTileFragmentPresenterFactory
import javax.inject.Inject

internal final class DashboardTabFragment : Fragment() {
    private var presenter: DashboardTabFragmentPresenter? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var dashboardService: IDashboardService

    @Inject
    public lateinit var dashboardTileFragmentPresenterFactory: IDashboardTileFragmentPresenterFactory

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activityrecorder_dashboard_tab_fragment, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DashboardTabFragmentPresenter(this, this.dashboardService, this.dashboardTileFragmentPresenterFactory)
    }
}

