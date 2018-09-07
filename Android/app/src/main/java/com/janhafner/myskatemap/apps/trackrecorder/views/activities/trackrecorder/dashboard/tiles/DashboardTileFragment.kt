package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject


internal abstract class DashboardTileFragment : Fragment() {
    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var appSettings: IAppSettings

    protected var presenter: DashboardTileFragmentPresenter? = null

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = this.arguments!!.getInt(DashboardTileFragment.EXTRAS_LAYOUT_ARGUMENT_KEY)

        return inflater.inflate(layout, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = this.createPresenter()
    }

    protected abstract fun createPresenter() : DashboardTileFragmentPresenter

    public override fun onDestroyView() {
        this.presenter!!.destroy()

        super.onDestroyView()
    }

    public companion object {
        public val EXTRAS_LAYOUT_ARGUMENT_KEY: String = "EXTRAS_LAYOUT_ARGUMENT"
    }
}