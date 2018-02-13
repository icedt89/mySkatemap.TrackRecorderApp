package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import java.io.File
import java.lang.reflect.Type

internal final class CurrentTrackRecordingStore(context: Context, typeOfT: Type)
   : FileBasedDataStore<TrackRecording>(File(context.filesDir, "CurrentTrackRecording.json"), typeOfT) {
}