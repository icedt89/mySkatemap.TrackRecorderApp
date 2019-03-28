package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.settings.IUserProfileSettings
import javax.inject.Inject


internal final class ActivityRecorderActivity: AppCompatActivity() {
    @Inject
    public lateinit var activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var userProfileSettings: IUserProfileSettings

    private var presenter: ActivityRecorderActivityPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = ActivityRecorderActivityPresenter(this, this.activityRecorderServiceController, this.appSettings, this.userProfileSettings)
        this.presenter!!.handleIntent(this.intent)
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return this.presenter!!.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }

    public override fun onBackPressed() {
        if(this.presenter!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    public override fun onDestroy() {
        this.presenter!!.destroy()

        super.onDestroy()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        this.presenter!!.onActivityResult(requestCode, resultCode, data)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        this.presenter!!.handleIntent(intent)
    }
}