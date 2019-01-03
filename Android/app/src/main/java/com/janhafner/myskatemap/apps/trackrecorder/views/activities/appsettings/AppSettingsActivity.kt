package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

internal final class AppSettingsActivity : AppCompatActivity() {
    private lateinit var presenter: AppSettingsActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = AppSettingsActivityPresenter(this)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter.onOptionsItemSelected(item)
    }
}

