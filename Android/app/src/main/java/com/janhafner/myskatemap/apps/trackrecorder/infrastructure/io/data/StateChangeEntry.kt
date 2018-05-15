package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import org.joda.time.DateTime

internal final class StateChangeEntry(public val at: DateTime, public val stateChangeReason: StateChangeReason) {
    public fun pause(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), StateChangeReason.Paused)
    }

    public fun resume(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), StateChangeReason.Running)
    }

    public fun finish(at: DateTime): StateChangeEntry {
        return StateChangeEntry(at, StateChangeReason.Finished)
    }

    companion object {
        public fun start(at: DateTime): StateChangeEntry {
            return StateChangeEntry(at, StateChangeReason.Running)
        }
    }
}

