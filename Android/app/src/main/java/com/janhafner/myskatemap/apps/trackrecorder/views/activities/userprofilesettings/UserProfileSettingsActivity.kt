package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

internal final class UserProfileSettingsActivity : AppCompatActivity() {
    private lateinit var presenter: UserProfileSettingsActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = UserProfileSettingsActivityPresenter(this)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter.onOptionsItemSelected(item)
    }
}

