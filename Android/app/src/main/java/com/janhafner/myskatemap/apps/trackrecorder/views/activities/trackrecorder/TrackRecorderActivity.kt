package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfile
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import javax.inject.Inject


internal final class TrackRecorderActivity: AppCompatActivity(), INeedFragmentVisibilityInfo {
    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var userProfile: IUserProfile

    private var presenter: TrackRecorderActivityPresenter? = null

    public override fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean) {
        this.presenter?.onFragmentVisibilityChange(fragment, isVisibleToUser)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackRecorderActivityPresenter(this, this.trackService, this.trackRecorderServiceController, this.appSettings, this.userProfile)
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Setup of FAB and existing session must be done after the options menu was created! (DIRTY!)
        return this.presenter!!.onCreateOptionsMenu(menu)
    }

    public override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        return this.presenter!!.onMenuOpened()
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        this.presenter!!.destroy()

        super.onDestroy()
    }
}