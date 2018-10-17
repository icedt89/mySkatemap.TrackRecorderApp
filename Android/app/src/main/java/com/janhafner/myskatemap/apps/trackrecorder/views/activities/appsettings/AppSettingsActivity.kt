package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

internal final class AppSettingsActivity : AppCompatActivity() {
    private var presenter: AppSettingsActivityPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = AppSettingsActivityPresenter(this)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }
}

