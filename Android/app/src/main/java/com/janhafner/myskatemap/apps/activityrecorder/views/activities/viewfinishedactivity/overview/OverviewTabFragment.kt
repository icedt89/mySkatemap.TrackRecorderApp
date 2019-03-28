package com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.ViewFinishedActivityActivity
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


internal final class OverviewTabFragment: Fragment() {
    private lateinit var presenter: OverviewTabFragmentPresenter

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var speedConverterFactory: ISpeedConverterFactory

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.context!!.getApplicationInjector().inject(this)

        this.presenter = OverviewTabFragmentPresenter(this, this.speedConverterFactory, this.distanceConverterFactory, this.appSettings)

        return inflater.inflate(R.layout.viewfinishedactivity_overview_tab_fragment, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewFinishedTrackActivity = this.activity as ViewFinishedActivityActivity
        viewFinishedTrackActivity.activityLoader
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if(it.value != null) {
                        this.presenter.setActivity(it.value!!)
                    } else {
                        // TODO: FEHLERBEHANDLUNG
                    }
                }
    }
}