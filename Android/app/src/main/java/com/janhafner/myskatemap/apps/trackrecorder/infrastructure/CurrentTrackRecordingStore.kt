package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import java.io.File

internal final class CurrentTrackRecordingStore(context: Context)
   : FileBasedDataStore<TrackRecording>(File(context.filesDir, "CurrentTrackRecording.json")) {
}