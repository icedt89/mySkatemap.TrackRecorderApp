package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class PlaygroundActivityPresenter(private val view: PlaygroundActivity) {
    init {
        this.view.setContentView(R.layout.activity_playground)
    }

    public fun destroy() {
    }
}