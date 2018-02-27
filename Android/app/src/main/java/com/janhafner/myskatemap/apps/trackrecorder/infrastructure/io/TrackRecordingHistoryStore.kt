package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.data.HistoricTrackRecording
import java.io.File

internal final class TrackRecordingHistoryStore(context: Context)
    : FileBasedDataStore<List<HistoricTrackRecording>>(File(context.filesDir, TrackRecordingHistoryStore.FILENAME), List::class.java) {

    companion object {
        public const val FILENAME: String = "TrackRecordingHistory.json"
    }
}