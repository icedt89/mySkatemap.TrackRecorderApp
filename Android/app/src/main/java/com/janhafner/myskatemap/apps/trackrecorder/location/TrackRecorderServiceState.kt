package com.janhafner.myskatemap.apps.trackrecorder.location

internal enum class TrackRecorderServiceState{
    Initializing,

    Ready,

    Running,

    Paused,

    LocationServicesUnavailable
}