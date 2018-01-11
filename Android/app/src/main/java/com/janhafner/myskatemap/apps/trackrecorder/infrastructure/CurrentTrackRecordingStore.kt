package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.CurrentTrackRecording
import java.io.File

internal final class CurrentTrackRecordingStore : FileBasedDataStore<CurrentTrackRecording> {
    public constructor(context: Context) : super(File(context.filesDir, "CurrentTrackRecording.json"));
}