package com.janhafner.myskatemap.apps.trackrecorder.live

import com.squareup.moshi.Moshi
import io.reactivex.Single

public final class HttpLiveSessionProvider(private val httpLiveSessionApiClient: HttpLiveSessionApiClient,
                                           private val moshi: Moshi): ILiveSessionProvider {
    public override fun createSession(): Single<ILiveSession> {
        return this.httpLiveSessionApiClient.post("")
                .map {
                    val adapter = this.moshi.adapter<LiveSessionCreatedResponse>(LiveSessionCreatedResponse::class.java)
                    val liveSessionCreatedResponse = adapter.fromJson(it.body()!!.source())!!

                    val sessionId = liveSessionCreatedResponse.sessionId

                    if(sessionId.isNullOrEmpty()) {
                        throw IllegalArgumentException("SessionId not available!")
                    }

                    HttpLiveSession(httpLiveSessionApiClient, sessionId, this.moshi)
                }
    }
}

