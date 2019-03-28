package com.janhafner.myskatemap.apps.activityrecorder.views.activities.appsettings

import android.view.MenuItem
import com.janhafner.myskatemap.apps.activityrecorder.R
import kotlinx.android.synthetic.main.app_toolbar.*

internal final class AppSettingsActivityPresenter(private val view: AppSettingsActivity) {
    init {
        this.view.setContentView(R.layout.appsettings_activity)

        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)

        this.view.supportFragmentManager.beginTransaction()
                .replace(R.id.settingsfragment_host, AppSettingsFragment())
                .commit()
    }

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.view.onBackPressed()
        }

        return true
    }
}