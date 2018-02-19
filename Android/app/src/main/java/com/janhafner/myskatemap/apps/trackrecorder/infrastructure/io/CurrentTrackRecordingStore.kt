package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import java.io.File

internal final class CurrentTrackRecordingStore(context: Context)
   : FileBasedDataStore<TrackRecording>(File(context.filesDir, "CurrentTrackRecording.json"), TrackRecording::class.java) {
}