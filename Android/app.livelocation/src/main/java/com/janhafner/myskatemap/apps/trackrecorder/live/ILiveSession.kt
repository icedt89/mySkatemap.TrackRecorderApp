package com.janhafner.myskatemap.apps.trackrecorder.live

import io.reactivex.Single

public interface ILiveSession {
    fun postLocations(locations: List<LiveLocation>): Single<Unit>

    fun close(): Single<Unit>
}