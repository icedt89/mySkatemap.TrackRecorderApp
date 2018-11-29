package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

internal final class UserProfileSettingsActivity : AppCompatActivity() {
    private var presenter: UserProfileSettingsActivityPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = UserProfileSettingsActivityPresenter(this)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }
}

