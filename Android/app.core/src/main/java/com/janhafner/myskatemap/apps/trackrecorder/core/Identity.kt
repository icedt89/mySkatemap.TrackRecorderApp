package com.janhafner.myskatemap.apps.trackrecorder.core

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

public open class Identity(public val displayName: String, public val username: String, protected val provider: String? = null) {
    public var accessToken: String? = null

    public val isAuthenticated: Boolean
        get() = !this.provider.isNullOrEmpty() && !this.accessToken.isNullOrEmpty()

    companion object {
        public fun anonymous(): Identity {
            return Identity("Anonymous", "(not set)")
        }
    }
}

public final class GoogleIdentity(displayName: String, username: String) : Identity(displayName, username, "Google") {
    public companion object {
        public fun trySignIn(context: Context): Identity? {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                return null
            }

            val displayName = account.displayName!!
            val username = account.email!!
            val accessToken = account.idToken!!

            val googleIdentity = GoogleIdentity(displayName, username)
            googleIdentity.accessToken = accessToken

            return googleIdentity
        }

        public fun fromIntent(intent: Intent): Identity {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(intent)

            val account = completedTask.getResult(ApiException::class.java)

            val displayName = account!!.displayName!!
            val username = account.email!!
            val accessToken = account.idToken!!

            val googleIdentity = GoogleIdentity(displayName, username)
            googleIdentity.accessToken = accessToken

            return googleIdentity
        }
    }
}