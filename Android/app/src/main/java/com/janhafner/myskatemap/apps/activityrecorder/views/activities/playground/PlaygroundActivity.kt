package com.janhafner.myskatemap.apps.activityrecorder.views.activities.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.activityrecorder.R

internal final class PlaygroundActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.playground_activity)
    }
}

