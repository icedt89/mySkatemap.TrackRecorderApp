package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.refactored.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.refactored.IFileAccessor
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import java.nio.ByteBuffer

internal interface ITrackService {
    fun hasCurrentTrackRecording(): Boolean

    fun getCurrentTrackRecording(): TrackRecording?

    fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording>

    fun saveTrackRecording(trackRecording: TrackRecording)
}

internal final class TrackService(private val appBaseDirectoryNavigator: IDirectoryNavigator, private val moshi: Moshi) : ITrackService {
    private val currentTrackRecordingFileAccessor: IFileAccessor

    init {
        this.currentTrackRecordingFileAccessor = this.appBaseDirectoryNavigator.getFile("currenttrackrecording.json")
    }

    private var currentTrackRecording: TrackRecording? = null

    public override fun hasCurrentTrackRecording(): Boolean {
        return this.currentTrackRecordingFileAccessor.exists()
    }

    public override fun getCurrentTrackRecording(): TrackRecording? {
        val rawCurrentTrackRecording = this.currentTrackRecordingFileAccessor.getContent()

        if(rawCurrentTrackRecording != null) {
            val adapter = this.moshi.adapter<TrackRecording>(TrackRecording::class.java)

            val buffer = Buffer().write(rawCurrentTrackRecording)

            this.currentTrackRecording = adapter.fromJson(buffer)
        }

        return this.currentTrackRecording
    }

    public override fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording> {
        TODO()
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording) {
        this.currentTrackRecording = trackRecording

        val adapter = this.moshi.adapter<TrackRecording>(TrackRecording::class.java)
        val json = adapter.toJson(this.currentTrackRecording)

        val buffer = Buffer().write(ByteString())

        this.currentTrackRecordingFileAccessor.saveContent(this.currentTrackRecording!!)
    }
}