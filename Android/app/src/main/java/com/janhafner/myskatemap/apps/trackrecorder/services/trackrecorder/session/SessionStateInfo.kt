package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackingPausedReason

internal final class SessionStateInfo(public val state: TrackRecordingSessionState, public val pausedReason: TrackingPausedReason? = null) {
}