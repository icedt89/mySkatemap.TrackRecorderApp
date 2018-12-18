package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingPausedReason

internal final class SessionStateInfo(public val state: TrackRecordingSessionState, public val pausedReason: TrackingPausedReason? = null) {
}