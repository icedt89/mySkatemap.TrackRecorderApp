package com.janhafner.myskatemap.apps.activityrecorder.views

import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

internal final class TabDefinition(public val pageTitle: String,
                                   public val tabFragmentFactory: () -> Fragment,
                                   public val position: Int,
                                   @DrawableRes public val iconResource: Int? = null,
                                   @LayoutRes public val customLayoutResource: Int? = null,
                                   public val isAvailable: Boolean = true) {
}