package com.janhafner.myskatemap.apps.trackrecorder.live

import android.util.Log
import io.reactivex.Single

public final class NullLiveSession(private val sessionId: String): ILiveSession {
    private var currentSessionId: String? = null

    init {
        this.currentSessionId = sessionId;
    }

    public override fun postLocations(locations: List<LiveLocation>): Single<Unit> {
        return Single.just(Unit)
                .doOnSubscribe {
                    if (this.currentSessionId != null) {
                        Log.d("NullLiveLocation", "Sending ${locations.count()} locations to the void")
                    }
                }
    }

    public override fun close(): Single<Unit> {
        return Single.just(Unit)
                .doOnSubscribe {
                    Log.d("NullLiveLocation", "Live session ${this.sessionId} closed")

                    this.currentSessionId = null
                }
    }
}