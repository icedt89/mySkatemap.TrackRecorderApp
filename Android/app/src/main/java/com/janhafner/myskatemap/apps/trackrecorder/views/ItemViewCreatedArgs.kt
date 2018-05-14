package com.janhafner.myskatemap.apps.trackrecorder.views

import android.view.View

internal final class ItemViewCreatedArgs<out TView: View, out TItem>(public val view: TView,
                                                                                       public val item: TItem,
                                                                                       public val position: Int)