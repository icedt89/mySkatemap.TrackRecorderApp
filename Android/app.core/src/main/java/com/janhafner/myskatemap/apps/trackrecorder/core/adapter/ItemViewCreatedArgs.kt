package com.janhafner.myskatemap.apps.trackrecorder.core.adapter

import android.view.View

public final class ItemViewCreatedArgs<out TView: View, out TItem>(public val view: TView,
                                                                                       public val item: TItem,
                                                                                       public val position: Int)