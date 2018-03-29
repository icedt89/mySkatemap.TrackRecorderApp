package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.squareup.moshi.Moshi
import java.io.File

internal final class CurrentTrackRecordingStore(context: Context, moshi: Moshi)
   : FileBasedDataStore<TrackRecording>(File(context.filesDir, CurrentTrackRecordingStore.FILENAME), TrackRecording::class.java, moshi) {

   companion object {
       public const val FILENAME: String = "CurrentTrackRecording.json"
   }
}