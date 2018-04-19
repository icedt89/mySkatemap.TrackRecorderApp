package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileAccessor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer

internal final class TrackService(private val appBaseDirectoryNavigator: IDirectoryNavigator, private val moshi: Moshi) : ITrackService {
    private val currentTrackRecordingFileAccessor: IFileAccessor

    private val currentTrackRecordingAttachmentsDirectoryNavigator: IDirectoryNavigator

    private val trackRecordingAdapter: JsonAdapter<TrackRecording> = this.moshi.adapter<TrackRecording>(TrackRecording::class.java)

    private var currentTrackRecordingLazy: Lazy<TrackRecording?> = lazy {
        this.getCurrentTrackRecordingOrNull()
    }

    init {
        val recordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(TrackService.RECORDINGS_BASEDIRECTORY_NAME)
        val currentTrackRecordingDirectoryNavigator = recordingsDirectoryNavigator.getDirectory(TrackService.CURRENT_RECORDINGDIRECTORY_NAME)
        this.currentTrackRecordingFileAccessor = currentTrackRecordingDirectoryNavigator.getFile(TrackService.RECORDING_DATAFILE_NAME)
        this.currentTrackRecordingAttachmentsDirectoryNavigator = currentTrackRecordingDirectoryNavigator.getDirectory(TrackService.RECORDING_ATTACHMENTS_DIRECTORYNAME)
    }

    public override fun hasCurrentTrackRecording(): Boolean {
        return this.currentTrackRecordingLazy.value != null
    }

    public override fun getCurrentTrackRecording(): TrackRecording {
        return this.currentTrackRecordingLazy.value!!
    }

    private fun getCurrentTrackRecordingOrNull(): TrackRecording? {
        val rawCurrentTrackRecording = this.currentTrackRecordingFileAccessor.getContent()

        if(rawCurrentTrackRecording != null) {
            val buffer = ByteBuffer.wrap(rawCurrentTrackRecording)
            val json = ByteString.of(buffer).utf8()

            return this.trackRecordingAdapter.fromJson(json)
        }

        return null
    }

    public override fun saveCurrentTrackRecording() {
        val json = this.trackRecordingAdapter.toJson(this.currentTrackRecordingLazy.value!!)

        val byteString = ByteString.encodeUtf8(json)
        val buffer = byteString.toByteArray()

        this.currentTrackRecordingFileAccessor.saveContent(buffer)
    }

    public override fun saveAsCurrentTrackRecording(trackRecording: TrackRecording) {
        this.currentTrackRecordingLazy = lazy {
            trackRecording
        }

        this.saveCurrentTrackRecording()
    }

    public override fun deleteCurrentTrackRecording() {
        this.currentTrackRecordingFileAccessor.delete()

        this.currentTrackRecordingLazy = lazy {
            this.getCurrentTrackRecordingOrNull()
        }
    }

    public override fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording> {
        TODO()
    }

    companion object {
        private const val RECORDING_DATAFILE_NAME: String = "data.json"
        private const val RECORDING_ATTACHMENTS_DIRECTORYNAME: String = "attachments"
        private const val RECORDINGS_BASEDIRECTORY_NAME: String = "recordings"
        private const val CURRENT_RECORDINGDIRECTORY_NAME: String = "current"
    }
}