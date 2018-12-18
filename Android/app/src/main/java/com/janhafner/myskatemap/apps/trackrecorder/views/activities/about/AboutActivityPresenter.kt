package com.janhafner.myskatemap.apps.trackrecorder.views.activities.about

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.getClipboardManager
import com.janhafner.myskatemap.apps.trackrecorder.getManifestVersionName
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_about.*


internal final class AboutActivityPresenter(private val view: AboutActivity) {
    private val subscriptions = CompositeDisposable()

    init {
        this.view.setContentView(R.layout.activity_about)

        val versionName = this.view.getManifestVersionName()
        this.view.activity_about_version.text = this.view.getString(R.string.aboutactivity_version, versionName)

        if (BuildConfig.DEBUG) {
            val processId = android.os.Process.myPid().toString()
            this.view.activity_about_app_pid.text = this.view.getString(R.string.aboutactivity_app_pid, processId)

            this.subscriptions.add(
                    this.view.activity_about_app_pid.longClicks().subscribe {
                        this.view.getClipboardManager().primaryClip = ClipData.newPlainText("PID", processId)

                        Toast.makeText(this.view, this.view.getString(R.string.aboutactivity_app_pid_copied_toast), Toast.LENGTH_SHORT).show()
                    })

            this.view.activity_about_app_pid.visibility = View.VISIBLE
        }

        this.subscriptions.add(
                this.view.activity_about_website.clicks().subscribe {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://${this.view.activity_about_website.text}"))

                    if (intent.resolveActivity(this.view.packageManager) != null) {
                        this.view.startActivity(intent)
                    }
                }
        )
    }

    public fun destroy() {
        this.subscriptions.dispose()
    }
}