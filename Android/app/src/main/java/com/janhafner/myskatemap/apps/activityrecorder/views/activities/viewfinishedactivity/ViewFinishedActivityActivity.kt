package com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.activityrecorder.core.Optional
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityService
import io.reactivex.Observable
import javax.inject.Inject


internal final class ViewFinishedActivityActivity: AppCompatActivity() {
    private var presenter: ViewFinishedActivityActivityPresenter? = null

    @Inject
    public lateinit var activityService: IActivityService

    public lateinit var activityLoader: Observable<Optional<Activity>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        val activityInfo = this.intent.getParcelableExtra<ActivityInfo>(ViewFinishedActivityActivityPresenter.EXTRA_ACTIVITY_KEY)
        this.activityLoader = this.activityService.getActivityByIdOrNull(activityInfo.id.toString())
                .toObservable()
                .replay(1)
                .autoConnect()

        this.presenter = ViewFinishedActivityActivityPresenter(this)

        super.onCreate(savedInstanceState)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        this.presenter!!.onActivityResult(requestCode, resultCode, data)
    }
}