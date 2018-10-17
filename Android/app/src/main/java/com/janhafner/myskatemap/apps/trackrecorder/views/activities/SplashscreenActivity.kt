package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

internal final class SplashscreenActivity: AppCompatActivity() {
    private lateinit var presenter: SplashscreenActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = SplashscreenActivityPresenter(this)
    }
}