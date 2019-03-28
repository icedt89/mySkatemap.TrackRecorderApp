package com.janhafner.myskatemap.apps.activityrecorder.live

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// TODO: Extract to common API
internal final class BearerCredentials {
    companion object {
        fun create(accessToken: String): String {
            return "Bearer ${accessToken}"
        }
    }
}

// TODO: Extract to common API
public interface IAccessTokenStore {
    fun getToken(purpose: String): String?
}

// TODO: Extract to common API
internal final class BearerAuthenticator(private val accessTokenStore: IAccessTokenStore, private val purpose: String) : Authenticator {
    public override fun authenticate(route: Route, response: Response): Request? {
        if (response.request().header("Authorization") != null) {
            return null
        }

        val accessToken = this.accessTokenStore.getToken(this.purpose)
        if (accessToken == null) {
            return null
        }

        val credential = BearerCredentials.create(accessToken)
        return response.request().newBuilder()
                .header("Authorization", credential)
                .build()
    }
}