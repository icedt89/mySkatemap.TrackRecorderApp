package com.janhafner.myskatemap.apps.trackrecorder.location

internal enum class TrackRecorderServiceState{
    Idle,

    Running,

    Paused,

    LocationServicesUnavailable
}