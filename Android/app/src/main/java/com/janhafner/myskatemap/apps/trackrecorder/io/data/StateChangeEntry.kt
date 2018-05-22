package com.janhafner.myskatemap.apps.trackrecorder.io.data

import com.couchbase.lite.Dictionary
import com.couchbase.lite.MutableDictionary
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

    public fun toCouchDbDictionary(): Dictionary {
        val result = MutableDictionary()

        result.setDate("at", this.at.toDate())
        result.setString("stateChangeReason", this.stateChangeReason.toString())

        return result
    }

    companion object {
        public fun start(at: DateTime): StateChangeEntry {
            return StateChangeEntry(at, StateChangeReason.Running)
        }

        public fun fromCouchDbDictionary(dictionary: Dictionary) : StateChangeEntry {
            val at = DateTime(dictionary.getDate("at"))
            val stateChangeReason = StateChangeReason.valueOf(dictionary.getString("stateChangeReason"))

            return StateChangeEntry(at, stateChangeReason)
        }
    }
}

