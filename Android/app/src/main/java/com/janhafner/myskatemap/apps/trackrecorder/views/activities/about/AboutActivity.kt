package com.janhafner.myskatemap.apps.trackrecorder.views.activities.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector

internal final class AboutActivity : AppCompatActivity() {
    public lateinit var presenter: AboutActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = AboutActivityPresenter(this)
    }
}