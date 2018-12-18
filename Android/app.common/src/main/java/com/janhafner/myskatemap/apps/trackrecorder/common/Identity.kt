package com.janhafner.myskatemap.apps.trackrecorder.common

public final class Identity(public val displayName: String, public val username: String, public val provider: String? = null) {
    public var accessToken: String? = null

    public val isAuthenticated: Boolean
        get() = !this.provider.isNullOrEmpty()

    companion object {
        public fun anonymous(): Identity {
            return Identity("Anonymous", "(not set)")
        }
    }
}