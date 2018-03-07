package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import io.reactivex.Observable

internal interface IAppSettingsChanged {
    val appSettingsChanged: Observable<PropertyChangedData>
}