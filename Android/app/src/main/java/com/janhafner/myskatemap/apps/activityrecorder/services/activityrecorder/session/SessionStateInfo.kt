package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session

import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingPausedReason

internal final class SessionStateInfo(public val state: ActivitySessionState, public val pausedReason: TrackingPausedReason? = null) {
}