package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.Context
import java.io.File

internal final class TrackRecordingAttachmentsBaseDirectoryAccessor(context: Context)
    : DirectoryAccessor(File(context.getExternalFilesDir(null), TrackRecordingAttachmentsBaseDirectoryAccessor.DIRECTORYNAME), context.contentResolver) {

    companion object {
        public const val DIRECTORYNAME: String = "Attachments"
    }
}