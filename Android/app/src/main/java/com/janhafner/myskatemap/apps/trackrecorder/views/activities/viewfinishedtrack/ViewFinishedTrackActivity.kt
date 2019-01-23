package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


internal final class ViewFinishedTrackActivity: AppCompatActivity() {
    private var presenter: ViewFinishedTrackActivityPresenter? = null

    @Inject
    public lateinit var trackService: ITrackService

    private var trackRecording: TrackRecording? = null

    private val fragments = mutableListOf<Fragment>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        val trackInfo = this.intent.getParcelableExtra<TrackInfo>(ViewFinishedTrackActivityPresenter.EXTRA_TRACK_RECORDING_KEY)
        this.trackService.getTrackRecordingByIdOrNull(trackInfo.id.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    result ->
                    if(result.value != null) {
                        this.trackRecording = result.value

                        for (fragment in this.fragments) {
                            if(fragment is INeedInputTrackRecording){
                                fragment.setTrackRecording(this.trackRecording!!)
                            }
                        }
                    } else {
                        // TODO: FEHLERBEHANDLUNG
                    }
                }

        super.onCreate(savedInstanceState)

        this.presenter = ViewFinishedTrackActivityPresenter(this)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if (fragment == null) {
            return
        }

        this.fragments.add(fragment)

        if (this.trackRecording != null) {
            if(fragment is INeedInputTrackRecording){
                fragment.setTrackRecording(this.trackRecording!!)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        this.presenter!!.onActivityResult(requestCode, resultCode, data)
    }
}