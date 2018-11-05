package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.live.ILiveSession
import com.janhafner.myskatemap.apps.trackrecorder.live.ILiveSessionProvider
import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation

internal final class LiveSessionController(private val liveSessionProvider: ILiveSessionProvider) : ILiveSessionController {
    private var currentLiveSession: ILiveSession? = null

    public override fun startSession() {
        if (this.currentLiveSession != null) {
            this.currentLiveSession!!.close().subscribe({
                _ ->
                this.currentLiveSession = null

                this.liveSessionProvider.createSession().subscribe {
                    liveSession ->
                    this.currentLiveSession = liveSession
                }
            }, {
                Log.i("LiveSessionController", "Closing live session failed: ${it}")
            })
        } else {
            this.liveSessionProvider.createSession().subscribe ({
                liveSession ->
                this.currentLiveSession = liveSession
            }, {

            })
        }
    }

    public override fun sendLocations(locations: List<LiveLocation>) {
        if (this.currentLiveSession != null) {
            this.currentLiveSession!!.postLocations(locations).subscribe({
            }, {
                Log.i("LiveSessionController", "Sending live locations to current session failed: ${it}")
            })
        } else {
            Log.d("LiveSessionController", "Send locations was called without a current session!")
        }
    }

    public override fun endSession() {
        if (this.currentLiveSession != null) {
            this.currentLiveSession!!.close().subscribe({
                this.currentLiveSession = null
            }, {
                Log.i("LiveSessionController", "Closing live session failed: ${it}")
            })
        }
    }
}