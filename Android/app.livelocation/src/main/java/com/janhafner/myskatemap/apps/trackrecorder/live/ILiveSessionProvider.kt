package com.janhafner.myskatemap.apps.trackrecorder.live

import io.reactivex.Single

public interface ILiveSessionProvider {
    fun createSession(): Single<ILiveSession>
}

