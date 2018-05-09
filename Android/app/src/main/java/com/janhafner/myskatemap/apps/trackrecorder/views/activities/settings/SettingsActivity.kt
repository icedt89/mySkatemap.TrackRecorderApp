package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

internal final class SettingsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}