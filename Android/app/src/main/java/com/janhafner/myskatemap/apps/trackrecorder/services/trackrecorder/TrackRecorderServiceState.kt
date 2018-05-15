package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

internal enum class TrackRecorderServiceState{
    Idle,

    Running,

    Paused,

    LocationServicesUnavailable
}