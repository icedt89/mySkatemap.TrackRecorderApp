package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

internal final class SessionStateInfo(public val state: TrackRecordingSessionState, public val pausedReason: TrackingPausedReason? = null) {
}