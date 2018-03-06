package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.view.View

internal final class ItemViewCreatedArgs<out TView: View, out TItem: Any>(public val view: TView,
                                                                                       public val item: TItem,
                                                                                       public val position: Int) {
}