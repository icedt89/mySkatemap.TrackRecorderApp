package com.janhafner.myskatemap.apps.trackrecorder.views

import android.support.v4.app.Fragment

internal interface INeedFragmentVisibilityInfo {
    fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean)
}