package com.janhafner.myskatemap.apps.trackrecorder

internal final class ViewHolder {
    private val viewCache: kotlin.collections.HashMap<Any, Any> = HashMap<Any, Any>()

    public fun store(key: Any, cachable: Any) {
        this.viewCache.put(key, cachable)
    }

    public fun clear() {
        this.viewCache.clear()
    }

    public fun remove(key: Any) {
        this.viewCache.remove(key)
    }

    public fun <T> retrieve(key: Any): T {
        return this.viewCache[key] as T
    }
}