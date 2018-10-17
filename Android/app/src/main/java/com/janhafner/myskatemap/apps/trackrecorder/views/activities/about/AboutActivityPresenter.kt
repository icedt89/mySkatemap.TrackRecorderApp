package com.janhafner.myskatemap.apps.trackrecorder.views.activities.about

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getManifestVersionName
import kotlinx.android.synthetic.main.activity_about.*

internal final class AboutActivityPresenter(private val view: AboutActivity) {
    init {
        this.view.setContentView(R.layout.activity_about)

        val versionName = this.view.getManifestVersionName()

        this.view.activity_about_version.text = this.view.getString(R.string.aboutactivity_version, versionName)
    }
}