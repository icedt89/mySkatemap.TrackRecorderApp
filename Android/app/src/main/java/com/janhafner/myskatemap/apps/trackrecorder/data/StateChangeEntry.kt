package com.janhafner.myskatemap.apps.trackrecorder.data

import org.joda.time.DateTime

internal final class StateChangeEntry(public val at: DateTime, public val paused: Boolean, public val finished: Boolean) {
    public fun paused(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), true, false)
    }

    public fun resumed(): StateChangeEntry {
        return StateChangeEntry(DateTime.now(), false, false)
    }

    public fun finished(at: DateTime): StateChangeEntry {
        return StateChangeEntry(at, false, true)
    }

    companion object {
        public fun started(at: DateTime): StateChangeEntry {
            return StateChangeEntry(at, false, false)
        }
    }
}

