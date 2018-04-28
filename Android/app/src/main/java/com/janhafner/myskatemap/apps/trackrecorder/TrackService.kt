package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileAccessor
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer
import java.util.*

internal final class TrackService(private val appBaseDirectoryNavigator: IDirectoryNavigator, private val moshi: Moshi) : ITrackService {
    private val currentTrackRecordingFileAccessor: IFileAccessor

    private val currentTrackRecordingAttachmentsDirectoryNavigator: IDirectoryNavigator

    private val trackRecordingsDirectoryNavigator: IDirectoryNavigator

    private val trackRecordingAdapter: JsonAdapter<TrackRecording> = this.moshi.adapter<TrackRecording>(TrackRecording::class.java)

    private var currentTrackRecordingLazy: Lazy<TrackRecording?> = lazy {
        this.getCurrentTrackRecordingOrNull()
    }

    init {
        this.trackRecordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(TrackService.RECORDINGS_BASEDIRECTORY_NAME)
        val currentTrackRecordingDirectoryNavigator = this.trackRecordingsDirectoryNavigator.getDirectory(TrackService.CURRENT_RECORDINGDIRECTORY_NAME)

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
        val dataFileContent = this.currentTrackRecordingFileAccessor.getContent()

        if(dataFileContent != null) {
            return this.getTrackRecording(dataFileContent)
        }

        return null
    }

    private fun getTrackRecording(content: ByteArray) : TrackRecording {
        val buffer = ByteBuffer.wrap(content)
        val json = ByteString.of(buffer).utf8()

        return this.trackRecordingAdapter.fromJson(json)!!
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
        val directories = this.trackRecordingsDirectoryNavigator.getDirectories()

        val result = ArrayList<TrackRecording>()

        directories.forEach {
            if (!it.name.equals(CURRENT_RECORDINGDIRECTORY_NAME, true) || includeCurrent) {
                val dataFile = it.getFile(RECORDING_DATAFILE_NAME)
                val dataFileContent = dataFile.getContent()
                if(dataFileContent != null) {
                    val trackRecording = this.getTrackRecording(dataFileContent)

                    result.add(trackRecording)
                }
            }
        }

        return result
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording) {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(trackRecording.id.toString())

        val dataFile = trackRecordingDirectoryAccessor.getFile(RECORDING_DATAFILE_NAME)

        val json = this.trackRecordingAdapter.toJson(this.currentTrackRecordingLazy.value!!)

        val byteString = ByteString.encodeUtf8(json)
        val buffer = byteString.toByteArray()

        dataFile.saveContent(buffer)
    }

    public override fun deleteTrackRecording(id: UUID) {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id.toString())

        trackRecordingDirectoryAccessor.delete()
    }

    public override fun hasTrackRecording(id: UUID): Boolean {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id.toString())

        return trackRecordingDirectoryAccessor.exists()
    }

    public override fun getTrackRecording(id: UUID): TrackRecording {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id.toString())

        val dataFile = trackRecordingDirectoryAccessor.getFile(RECORDING_DATAFILE_NAME)

        val dataFileContent = dataFile.getContent()!!

        return this.getTrackRecording(dataFileContent)
    }

    companion object {
        private const val RECORDING_DATAFILE_NAME: String = "data.json"
        private const val RECORDING_ATTACHMENTS_DIRECTORYNAME: String = "attachments"
        private const val RECORDINGS_BASEDIRECTORY_NAME: String = "recordings"
        private const val CURRENT_RECORDINGDIRECTORY_NAME: String = "current"
    }
}