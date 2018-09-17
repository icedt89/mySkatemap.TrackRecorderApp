package com.janhafner.myskatemap.apps.trackrecorder.views.activities.about

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

internal final class AboutActivity : AppCompatActivity() {
    public lateinit var presenter: AboutActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = AboutActivityPresenter(this)
    }
}