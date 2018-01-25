package com.janhafner.myskatemap.apps.trackrecorder.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class TrackAttachmentsActivity: AppCompatActivity() {
    private var viewModel : TrackAttachmentsActivityViewModel? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_track_attachments)

        val trackAttachmentsToolbar = this.findViewById<Toolbar>(R.id.trackattachmentsactivity_toolbar)
        this.setSupportActionBar(trackAttachmentsToolbar)

        this.viewModel = TrackAttachmentsActivityViewModel(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}