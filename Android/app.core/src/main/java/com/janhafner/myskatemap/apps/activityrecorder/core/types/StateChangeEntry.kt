package com.janhafner.myskatemap.apps.activityrecorder.core.types

import org.joda.time.DateTime

public final class StateChangeEntry(public val at: DateTime,
                                    public val stateChangeReason: StateChangeReason,
                                    public val pausedReason: TrackingPausedReason?,
                                    public val resumedReason: TrackingResumedReason?) {
    public fun pause(pausedReason: TrackingPausedReason): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), StateChangeReason.Paused, pausedReason, null)
    }

    public fun resume(resumedReason: TrackingResumedReason): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), StateChangeReason.Running, null, resumedReason)
    }

    public fun finish(at: DateTime): StateChangeEntry {
        return StateChangeEntry(at, StateChangeReason.Finished, null, null)
    }

    companion object {
        public fun start(at: DateTime): StateChangeEntry {
            return StateChangeEntry(at, StateChangeReason.Running, null, TrackingResumedReason.JustInitialized)
        }
    }
}

