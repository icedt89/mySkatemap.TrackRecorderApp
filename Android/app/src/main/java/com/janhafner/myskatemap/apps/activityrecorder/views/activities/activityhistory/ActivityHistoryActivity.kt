package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityhistory

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityQueryService
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityService
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import javax.inject.Inject


internal final class ActivityHistoryActivity: AppCompatActivity(){
    @Inject
    public lateinit var activityQueryService: IActivityQueryService

    @Inject
    public lateinit var activityService: IActivityService

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var notifier: INotifier

    private lateinit var presenter: ActivityHistoryActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = ActivityHistoryActivityPresenter(this, this.activityQueryService, this.distanceConverterFactory, this.appSettings, this.activityService, this.notifier)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter.onOptionsItemSelected(item)
    }
}