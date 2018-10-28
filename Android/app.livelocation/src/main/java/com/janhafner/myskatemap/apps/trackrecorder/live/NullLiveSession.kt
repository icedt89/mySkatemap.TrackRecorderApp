package com.janhafner.myskatemap.apps.trackrecorder.live

import android.util.Log
import io.reactivex.Single

public final class NullLiveSession(private val sessionId: String): ILiveSession {
    private var currentSessionId: String? = null

    init {
        this.currentSessionId = sessionId;
    }

    public override fun postLocations(locations: List<LiveLocation>): Single<Unit> {
        return Single.fromCallable {
            if (this.currentSessionId != null) {
                Log.d("NullLiveLocation", "Sending ${locations.count()} locations to the void")
            }

            Unit
        }
    }

    public override fun close(): Single<Unit> {
        return Single.fromCallable {
            Log.d("NullLiveLocation", "Closing session ${this.sessionId}")

            this.currentSessionId = null

            Unit
        }
    }
}