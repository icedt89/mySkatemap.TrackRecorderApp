package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.live.ILiveSession
import com.janhafner.myskatemap.apps.trackrecorder.live.ILiveSessionProvider
import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation
import io.reactivex.Single

internal final class LiveSessionController(private val liveSessionProvider: ILiveSessionProvider) : ILiveSessionController {
    private var currentLiveSession: ILiveSession? = null

    public override fun startSession(): Single<Unit> {
        if (this.currentLiveSession != null) {
            return this.currentLiveSession!!.close()
                    .doFinally {
                        this.currentLiveSession = null
                    }
                    .doOnError {
                        Log.e("LiveSessionController", "Closing live session failed: ${it}")
                    }
                    .flatMap {
                        this.liveSessionProvider.createSession()
                    }
                    .doOnSuccess {
                        this.currentLiveSession = it
                    }
                    .doOnError {
                        Log.e("LiveSessionController", "Creating live session failed: ${it}")
                    }
                    .map { }
        }

        return this.liveSessionProvider.createSession()
                .doOnSuccess {
                    this.currentLiveSession = it
                }
                .doOnError {
                    Log.e("LiveSessionController", "Creating live session failed: ${it}")
                }
                .map {}
    }

    public override fun sendLocations(locations: List<LiveLocation>): Single<Unit> {
        if (this.currentLiveSession != null) {
            return this.currentLiveSession!!.postLocations(locations)
                    .doOnError {
                        Log.e("LiveSessionController", "Sending live locations to current session failed: ${it}")
                    }
        }

        return Single.just(Unit)
                .doFinally {
                    Log.d("LiveSessionController", "Send locations was called without a current live session")
                }
    }

    public override fun endSession(): Single<Unit> {
        if (this.currentLiveSession != null) {
            return this.currentLiveSession!!.close()
                    .doFinally {
                        this.currentLiveSession = null
                    }
                    .doOnError {
                        Log.e("LiveSessionController", "Closing live session failed: ${it}")
                    }
        }

        return Single.just(Unit)
                .doFinally {
                    Log.d("LiveSessionController", "Close was called without a current live session")
                }
    }
}