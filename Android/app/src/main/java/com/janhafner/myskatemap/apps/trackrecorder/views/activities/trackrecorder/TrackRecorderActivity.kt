package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored.RefactoredTrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import javax.inject.Inject


internal final class TrackRecorderActivity: AppCompatActivity(), INeedFragmentVisibilityInfo {
    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<RefactoredTrackRecorderService, TrackRecorderServiceBinder>

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var appSettings: IAppSettings

    private var presenter: TrackRecorderActivityPresenter? = null

    public override fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean) {
        this.presenter?.onFragmentVisibilityChange(fragment, isVisibleToUser)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackRecorderActivityPresenter(this, this.trackService, this.trackRecorderServiceController, this.appSettings)
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return this.presenter!!.onCreateOptionsMenu(menu)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.presenter!!.onActivityResult(requestCode, resultCode, data)
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.presenter!!.destroy()
    }
}