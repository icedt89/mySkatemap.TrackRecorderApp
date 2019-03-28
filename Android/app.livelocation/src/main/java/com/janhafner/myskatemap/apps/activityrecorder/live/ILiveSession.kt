package com.janhafner.myskatemap.apps.activityrecorder.live

import io.reactivex.Single

public interface ILiveSession {
    fun postLocations(locations: List<LiveLocation>): Single<Unit>

    fun close(): Single<Unit>
}