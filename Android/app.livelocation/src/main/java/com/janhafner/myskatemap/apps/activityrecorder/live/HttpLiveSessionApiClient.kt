package com.janhafner.myskatemap.apps.activityrecorder.live

import io.reactivex.Single
import okhttp3.*

public final class HttpLiveSessionApiClient(private val httpClient: OkHttpClient, private val baseUrl: String) {
    private val jsonMediaType: String = "application/json; charset=utf-8"

    public fun post(body: String): Single<Response> {
        val body = RequestBody.create(MediaType.parse(this.jsonMediaType), body)

        val request = Request.Builder()
                .post(body)
                .url("${this.baseUrl}/live")
                .build()

        val call = this.httpClient.newCall(request)

        return call.toSingle()
    }

    public fun put(sessionId: String, body: String): Single<Response> {
        val body = RequestBody.create(MediaType.parse(this.jsonMediaType), body)

        val request = Request.Builder()
                .put(body)
                .url("${this.baseUrl}/live/${sessionId}/locations")
                .build()

        val call = this.httpClient.newCall(request)

        return call.toSingle()
    }

    public fun delete(sessionId: String): Single<Response> {
        val request = Request.Builder()
                .delete()
                .url("${this.baseUrl}/live/${sessionId}")
                .build()

        val call = this.httpClient.newCall(request)

        return call.toSingle()
    }
}