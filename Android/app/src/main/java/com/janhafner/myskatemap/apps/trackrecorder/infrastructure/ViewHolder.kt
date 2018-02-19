package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.support.v4.util.ArrayMap

internal final class ViewHolder {
    private val viewCache: MutableMap<Any, Any> = ArrayMap<Any, Any>()

    public fun store(key: Any, cachable: Any) {
        this.viewCache[key] = cachable
    }

    public fun clear() {
        this.viewCache.clear()
    }

    public fun remove(key: Any) {
        this.viewCache.remove(key)
    }

    public fun <T> retrieve(key: Any): T {
        return this.viewCache.get(key) as T
    }

    public fun <T> tryRetrieve(key: Any): T? {
        if(this.viewCache.containsKey(key)) {
            return this.viewCache.get(key) as T?
        }

        return null
    }
}