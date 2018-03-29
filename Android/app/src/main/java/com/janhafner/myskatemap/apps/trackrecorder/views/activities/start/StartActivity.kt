package com.janhafner.myskatemap.apps.trackrecorder.views.activities.start

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import java.util.*
import javax.inject.Inject


internal final class StartActivity: AppCompatActivity() {
    @Inject
    public lateinit var currentTrackRecordingStore: IFileBasedDataStore<TrackRecording>

    @Inject
    public lateinit var appSettings: IAppSettings

    public override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.settings, true)

        this.getApplicationInjector().inject(this)

        // TODO: Right place?
        val currentLocale = Locale(this.appSettings.appUiLocale)
        Locale.setDefault(currentLocale)

        super.onCreate(savedInstanceState)

        val currentTrackRecording = this.currentTrackRecordingStore.getData()
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