package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class PlaygroundFragment : Fragment() {
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_playground, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}