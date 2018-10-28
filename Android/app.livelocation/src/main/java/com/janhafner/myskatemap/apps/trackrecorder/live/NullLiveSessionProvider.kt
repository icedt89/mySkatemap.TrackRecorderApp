package com.janhafner.myskatemap.apps.trackrecorder.live

import android.util.Log
import io.reactivex.Single
import java.util.*

public final class NullLiveSessionProvider: ILiveSessionProvider {
    public override fun createSession(): Single<ILiveSession> {
        return Single.fromCallable {
            val sessionId = UUID.randomUUID().toString()

            Log.d("NullLiveLocation", "Created new session id: ${sessionId}")

            NullLiveSession(sessionId)
        }
    }
}