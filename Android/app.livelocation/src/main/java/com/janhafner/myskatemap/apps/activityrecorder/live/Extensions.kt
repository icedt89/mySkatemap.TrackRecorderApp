package com.janhafner.myskatemap.apps.activityrecorder.live

import io.reactivex.Single
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal fun Call.toSingle(): Single<Response> {
    return Single.fromPublisher {
        observer ->
        this.enqueue(object: Callback {
            public override fun onFailure(call: Call, e: IOException) {
                observer.onError(e)
            }

            public override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    observer.onNext(response)

                    observer.onComplete()
                } else {
                    observer.onError(Throwable(response.message()))
                }
            }
        })
    }
}
