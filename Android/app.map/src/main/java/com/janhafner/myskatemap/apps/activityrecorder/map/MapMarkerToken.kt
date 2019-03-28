package com.janhafner.myskatemap.apps.activityrecorder.map

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable

public final class MapMarkerToken(private val removeMarkerFunc: () -> Unit) : IDestroyable {
    private var isDestroyed = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.removeMarkerFunc()

        this.isDestroyed = true
    }
}