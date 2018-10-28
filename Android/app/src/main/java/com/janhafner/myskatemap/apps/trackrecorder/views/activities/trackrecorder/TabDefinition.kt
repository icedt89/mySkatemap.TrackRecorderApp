package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment

internal final class TabDefinition(public val pageTitle: String,
                                   public val tabFragmentFactory: () -> Fragment,
                                   public val position: Int,
                                   @DrawableRes public val iconResource: Int? = null,
                                   @LayoutRes public val customLayoutResource: Int? = null,
                                   public val isAvailable: Boolean = true) {
}