package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

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

