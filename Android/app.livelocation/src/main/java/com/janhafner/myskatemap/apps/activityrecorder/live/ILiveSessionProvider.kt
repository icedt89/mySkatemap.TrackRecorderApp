package com.janhafner.myskatemap.apps.activityrecorder.live

import io.reactivex.Single

public interface ILiveSessionProvider {
    fun createSession(): Single<ILiveSession>
}

