package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import io.reactivex.Observable
import javax.inject.Inject


internal final class ViewFinishedTrackActivity: AppCompatActivity() {
    private var presenter: ViewFinishedTrackActivityPresenter? = null

    @Inject
    public lateinit var trackService: ITrackService

    public lateinit var trackRecordingLoader: Observable<Optional<TrackRecording>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        val trackInfo = this.intent.getParcelableExtra<TrackInfo>(ViewFinishedTrackActivityPresenter.EXTRA_TRACK_RECORDING_KEY)
        this.trackRecordingLoader = this.trackService.getTrackRecordingByIdOrNull(trackInfo.id.toString())
                .toObservable()
                .replay(1)
                .autoConnect()

        this.presenter = ViewFinishedTrackActivityPresenter(this)

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