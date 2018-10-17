package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Single

public interface IMyLocationRequestState {
    val location: Single<Optional<Location>>

    fun cancel()
}