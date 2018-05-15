package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.SimpleLocation
import java.util.*

internal final class FakeLiveLocationTrackingService() : ILiveLocationTrackingService {
    public override fun createSession(): ILiveTrackingSession {
        val sessionId = UUID.randomUUID().toString()

        Log.i("FakeLiveTracking", "Session ${sessionId} was created")

        return FakeLiveTrackingSession(this, sessionId)
    }

    public fun sendLocations(sessionId: String, locations: List<SimpleLocation>) {
        Log.i("FakeLiveTracking", "Sent ${locations.count()} for session ${sessionId}")
    }

    public fun endSession(sessionId: String) {
        Log.i("FakeLiveTracking", "Session ${sessionId} has ended")
    }
}