package com.janhafner.myskatemap.apps.trackrecorder.views.userprofile.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

internal final class UserProfileSettingsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.fragmentManager.beginTransaction()
                .replace(android.R.id.content, UserProfileSettingsFragment())
                .commit()
    }
}