package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.ViewFinishedTrackActivity
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


internal final class MapTabFragment: Fragment() {
    private lateinit var presenter: MapTabFragmentPresenter

    @Inject
    public lateinit var appSettings: IAppSettings

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_viewfinishedtrack_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = MapTabFragmentPresenter(this, this.appSettings, false)

        val viewFinishedTrackActivity = this.activity as ViewFinishedTrackActivity
        viewFinishedTrackActivity.trackRecordingLoader
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if(it.value != null) {
                        this.presenter.setTrackRecording(it.value!!)
                    } else {
                        // TODO: FEHLERBEHANDLUNG
                    }
                }
    }
}