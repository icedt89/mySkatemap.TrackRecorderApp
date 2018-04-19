package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import org.joda.time.DateTime

internal final class StateChangeEntry(public val at: DateTime, public val state: TrackState) {
    public fun pause(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), TrackState.Paused)
    }

    public fun resume(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), TrackState.Running)
    }

    public fun finish(at: DateTime): StateChangeEntry {
        return StateChangeEntry(at, TrackState.Finished)
    }

    companion object {
        public fun start(at: DateTime): StateChangeEntry {
            return StateChangeEntry(at, TrackState.Running)
        }
    }
}

