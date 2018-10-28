package com.janhafner.myskatemap.apps.trackrecorder.live

import com.squareup.moshi.Moshi
import io.reactivex.Single

public final class HttpLiveSession(private val httpLiveSessionApiClient: HttpLiveSessionApiClient, private val sessionId: String, private val moshi: Moshi): ILiveSession {
    public override fun close(): Single<Unit> {
        return this.httpLiveSessionApiClient.delete(this.sessionId)
                .map {  }
    }

    public override fun postLocations(locations: List<LiveLocation>): Single<Unit> {
        val adapter = this.moshi.adapter<List<LiveLocation>>(locations.javaClass)

        val body = adapter.toJson(locations)

        return this.httpLiveSessionApiClient.put(this.sessionId, body)
                .map {  }
    }
}