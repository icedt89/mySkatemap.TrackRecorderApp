package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

internal enum class TrackingPausedReason {
    JustInitialized,

    UserInitiated,

    StillStandDetected,

    LocationServicesUnavailable
}