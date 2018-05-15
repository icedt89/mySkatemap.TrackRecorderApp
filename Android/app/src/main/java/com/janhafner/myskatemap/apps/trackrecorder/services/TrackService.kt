package com.janhafner.myskatemap.apps.trackrecorder.services

import com.janhafner.myskatemap.apps.trackrecorder.AttachmentHandler
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer
import java.util.*

internal final class TrackService(private val appBaseDirectoryNavigator: IDirectoryNavigator, private val moshi: Moshi) : ITrackService {
    private val trackRecordingsDirectoryNavigator: IDirectoryNavigator

    private val trackRecordingAdapter: JsonAdapter<TrackRecording> = this.moshi.adapter<TrackRecording>(TrackRecording::class.java)

    init {
        this.trackRecordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(RECORDINGS_BASEDIRECTORY_NAME)
    }

    public override fun getAllTrackRecordings(): List<TrackRecording> {
        val directories = this.trackRecordingsDirectoryNavigator.getDirectories()

        val result = ArrayList<TrackRecording>()

        directories.forEach {
            val dataFile = it.getFile(RECORDING_DATAFILE_NAME)
            val dataFileContent = dataFile.getContent()
            if(dataFileContent != null) {
                val trackRecording = this.getTrackRecording(dataFileContent)

                result.add(trackRecording)
            }
        }

        return result
    }

    public override fun hasTrackRecording(id: String): Boolean {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id.toString())

        val dataFile = trackRecordingDirectoryAccessor.getFile(RECORDING_DATAFILE_NAME)

        return dataFile.exists()
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording) {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(trackRecording.id.toString())

        val dataFile = trackRecordingDirectoryAccessor.getFile(RECORDING_DATAFILE_NAME)

        val json = this.trackRecordingAdapter.toJson(trackRecording)

        val byteString = ByteString.encodeUtf8(json)
        val buffer = byteString.toByteArray()

        dataFile.saveContent(buffer)
    }

    public override fun deleteTrackRecording(id: String) {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id)

        trackRecordingDirectoryAccessor.delete()
    }

    private fun getTrackRecording(content: ByteArray) : TrackRecording {
        val buffer = ByteBuffer.wrap(content)
        val json = ByteString.of(buffer).utf8()

        return this.trackRecordingAdapter.fromJson(json)!!
    }

    public override fun getTrackRecording(id: String): TrackRecording? {
        val trackRecordingDirectoryAccessor = this.trackRecordingsDirectoryNavigator.getDirectory(id)

        val dataFile = trackRecordingDirectoryAccessor.getFile(RECORDING_DATAFILE_NAME)

        val dataFileContent = dataFile.getContent()
        if(dataFileContent != null) {
            return this.getTrackRecording(dataFileContent)
        }

        return null
    }

    public override fun getAttachmentHandler(trackRecording: TrackRecording): AttachmentHandler {
        val directoryNavigator = this.trackRecordingsDirectoryNavigator
                .getDirectory(trackRecording.id.toString())
                .getDirectory(RECORDING_ATTACHMENTS_DIRECTORYNAME)

        return AttachmentHandler(trackRecording, directoryNavigator)
    }

    companion object {
        private const val RECORDING_DATAFILE_NAME: String = "data.json"
        private const val RECORDING_ATTACHMENTS_DIRECTORYNAME: String = "attachments"
        private const val RECORDINGS_BASEDIRECTORY_NAME: String = "recordings"
    }
}