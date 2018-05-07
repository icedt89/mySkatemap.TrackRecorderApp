package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import com.janhafner.myskatemap.apps.trackrecorder.delete
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.post
import com.janhafner.myskatemap.apps.trackrecorder.put
import com.squareup.moshi.Moshi
import okhttp3.*

internal final class JsonRestApiClient(private val okHttpClient: OkHttpClient, private val moshi: Moshi){
    public fun <T> toJsonRequestBody(body: T, typeClass: Class<T>) : RequestBody {
        val adapter = this.moshi.adapter(typeClass)

        val json = adapter.toJson(body)

        return RequestBody.create(MediaType.parse("application/json"), json)
    }

    public fun <T> fromJsonResponseBody(body: ResponseBody?, typeClass: Class<T>?): T? {
        if(body == null || typeClass == null) {
            return null
        }

        val adapter = this.moshi.adapter(typeClass)

        return adapter.fromJson(body.source())
    }

    public fun <TBody: Any> send(url: String, method: String, body: TBody? = null) : Response {
        val requestBuilder = Request.Builder()
                .url(url)

        var requestBody: RequestBody? = null
        if(body != null) {
            requestBody = this.toJsonRequestBody(body, body.javaClass)
        }

        requestBuilder.method(method, requestBody)

        val request = requestBuilder.build()

        return this.okHttpClient.newCall(request).execute()
    }

    public fun buildFullUrl(baseUrl: String, relativePart: String) : String {
        var realRelativePart = relativePart
        if(!baseUrl.endsWith("/") && !relativePart.startsWith("/")) {
            realRelativePart = "/${relativePart}"
        }

        return "${baseUrl}${realRelativePart}".replace("//", "/")
    }
}

internal final class LiveLocationTrackingService(private val jsonRestApiClient: JsonRestApiClient)
    : ILiveLocationTrackingService {
    private val baseUrl: String = "https://api.myskatemap.io"

    public override fun createSession(): ILiveTrackingSession {
        // POST /api/live
        // Server side: Store live session only in memory as long as endSession(...) is not called
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live")

        // TODO: Optain session id from response
        this.jsonRestApiClient.post(fullUrl, null)

        return LiveTrackingSession(this)
    }

    public fun sendLocations(sessionId: String, locations: ArrayList<SimpleLocation>) {
        // PUT /api/live/{sessiondId}
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live/${sessionId}")

        // TODO: Optain session id from response
        this.jsonRestApiClient.put(fullUrl, locations)
    }

    public fun endSession(sessionId: String) {
        // DELETE /api/live/{sessionId}
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live/${sessionId}")

        this.jsonRestApiClient.delete(fullUrl)
    }
}

internal interface ILiveLocationTrackingService {
    public fun createSession(): ILiveTrackingSession
}

internal final class LiveTrackingSession(private val liveLocationTrackingService: LiveLocationTrackingService) : ILiveTrackingSession {
    public override fun sendLocations(sessionId: String, locations: ArrayList<SimpleLocation>) {
        this.liveLocationTrackingService.sendLocations(sessionId, locations)
    }

    public override fun endSession(sessionId: String) {
        this.liveLocationTrackingService.endSession(sessionId)
    }
}

internal interface ILiveTrackingSession {
    fun sendLocations(sessionId: String, locations: ArrayList<SimpleLocation>)

    fun endSession(sessionId: String)
}
