package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import com.google.gson.Gson
import com.janhafner.myskatemap.apps.trackrecorder.data.HistoricTrackRecording
import java.io.File

internal final class TrackRecordingHistoryStore(context: Context, gson: Gson)
    : FileBasedDataStore<List<HistoricTrackRecording>>(File(context.filesDir, TrackRecordingHistoryStore.FILENAME), List::class.java, gson) {

    companion object {
        public const val FILENAME: String = "TrackRecordingHistory.json"
    }
}