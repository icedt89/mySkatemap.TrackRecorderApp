package com.janhafner.myskatemap.apps.trackrecorder

import okhttp3.Response

internal fun <TResponse> JsonRestApiClient.get(url: String, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "GET", null)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun JsonRestApiClient.delete(url: String): Response {
    return this.send(url, "DELETE", null)
}

internal fun <TRequest: Any, TResponse> JsonRestApiClient.post(url: String, body: TRequest, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "POST", body)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun <TRequest: Any> JsonRestApiClient.post(url: String, body: TRequest? = null): Response {
    return this.send(url, "POST", body)
}

internal fun <TRequest: Any, TResponse> JsonRestApiClient.put(url: String, body: TRequest, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "PUT", body)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun <TRequest: Any> JsonRestApiClient.put(url: String, body: TRequest? = null): Response {
    return this.send(url, "PUT", body)
}