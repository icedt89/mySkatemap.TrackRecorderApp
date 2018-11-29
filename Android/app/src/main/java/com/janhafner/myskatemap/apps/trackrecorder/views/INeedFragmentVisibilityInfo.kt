package com.janhafner.myskatemap.apps.trackrecorder.views

import androidx.fragment.app.Fragment

internal interface INeedFragmentVisibilityInfo {
    fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean)
}