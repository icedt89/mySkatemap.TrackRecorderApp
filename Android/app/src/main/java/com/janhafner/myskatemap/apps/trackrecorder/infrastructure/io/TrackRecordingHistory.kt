package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.data.HistoricTrackRecording
import java.io.File

internal final class TrackRecordingHistory(context: Context)
    : FileBasedDataStore<List<HistoricTrackRecording>>(File(context.filesDir, "TrackRecordingHistory.json"), List::class.java) {
}