package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import javax.inject.Inject


internal final class MapTabFragment: Fragment() {
    private var presenter: MapTabFragmentPresenter? = null

    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var trackRecorderMapFragment: TrackRecorderMapFragment

    @Inject
    public lateinit var appSettings: IAppSettings

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_trackrecorderactivity_map_tab, container, false)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        this.presenter?.setUserVisibleHint(isVisibleToUser)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = MapTabFragmentPresenter(this, this.trackRecorderServiceController, this.trackRecorderMapFragment, this.appSettings)
    }

    public override fun onAttach(context: Context?) {
        super.onAttach(context)

        if(this.activity is INeedFragmentVisibilityInfo) {
            (this.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this, true)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.presenter?.setUserVisibleHint(false)
    }
}