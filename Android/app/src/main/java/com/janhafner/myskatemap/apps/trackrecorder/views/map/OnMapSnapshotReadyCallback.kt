package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.graphics.Bitmap

internal interface OnMapSnapshotReadyCallback {
    fun onSnapshotReady(bitmap: Bitmap)
}