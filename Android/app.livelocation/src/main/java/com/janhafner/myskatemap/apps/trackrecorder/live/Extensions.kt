package com.janhafner.myskatemap.apps.trackrecorder.live

import io.reactivex.Single
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal fun Call.toSingle(): Single<Response> {
    return Single.fromPublisher{
        observer ->
        this.enqueue(object: Callback {
            public override fun onFailure(call: Call, e: IOException) {
                observer.onError(e)
            }

            public override fun onResponse(call: Call, response: Response) {
                observer.onNext(response)

                observer.onComplete()
            }
        })
    }
}
