package com.janhafner.myskatemap.apps.trackrecorder.settings

import io.reactivex.Observable

internal interface IAppSettingsChanged {
    val appSettingsChanged: Observable<PropertyChangedData>
}