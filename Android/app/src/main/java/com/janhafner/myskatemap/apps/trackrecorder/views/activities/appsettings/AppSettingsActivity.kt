package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.R
import kotlinx.android.synthetic.main.app_toolbar.*

internal final class AppSettingsActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_appsettings)

        this.setSupportActionBar(this.app_toolbar)

        this.fragmentManager.beginTransaction()
                .replace(R.id.settingsfragment_host, AppSettingsFragment())
                .commit()
    }
}