package com.janhafner.myskatemap.apps.trackrecorder.views.activities.start

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter

internal final class StartActivity: AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentTrackRecordingStore = CurrentTrackRecordingStore(this)

        val currentTrackRecording = currentTrackRecordingStore.getData()
        if(currentTrackRecording != null) {
            val intent = Intent(this, TrackRecorderActivity::class.java)
            intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.TryResume.toString())

            this.startActivity(intent, savedInstanceState)

            this.finish()
        } else {
            this.setContentView(R.layout.activity_start)

            val button = this.findViewById<ImageButton>(R.id.startactivity_button_start_new_recording)
            button.clicks().subscribe{
                val intent = Intent(this, TrackRecorderActivity::class.java)
                intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.StartNew.toString())

                this.startActivity(intent, savedInstanceState)

                this.finish()
            }
        }
    }
}