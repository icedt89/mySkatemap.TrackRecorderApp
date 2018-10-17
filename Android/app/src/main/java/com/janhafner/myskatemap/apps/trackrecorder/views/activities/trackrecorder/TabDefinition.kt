package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.support.v4.app.Fragment

internal final class TabDefinition(public val pageTitle: String, public val tabFragmentFactory: () -> Fragment, public val position: Int, public val isAvailable: Boolean = true) {
}