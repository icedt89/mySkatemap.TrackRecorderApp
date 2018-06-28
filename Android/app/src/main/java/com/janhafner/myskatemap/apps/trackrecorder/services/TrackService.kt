package com.janhafner.myskatemap.apps.trackrecorder.services

import android.util.Log
import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import java.util.*

internal final class TrackService(private val couchDb: Database,
                                  private val appBaseDirectoryNavigator: IDirectoryNavigator,
                                  private val appSettings: IAppSettings) : ITrackService {
    private val trackRecordingsDirectoryNavigator: IDirectoryNavigator

    init {
        this.trackRecordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(TrackService.RECORDING_BASE_DIRECTORY_NAME)
    }

    public override fun getAll(): List<TrackRecording> {
        val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                .from(DataSource.database(couchDb))
                .where(Expression.property("_id").isNot(Expression.value(this.appSettings.currentTrackRecordingId?.toString()))
                        .and(Expression.property("documentType").`is`(Expression.string(TrackRecording::javaClass.name))))

        val results = queryBuilder.execute()

        val trackRecordings = ArrayList<TrackRecording>()

        for (result in results) {
            val id = UUID.fromString(result.getString("id"))
            val dictionary = result.getDictionary(couchDb.name)

            try {
                val trackRecording = TrackRecording.fromCouchDbDictionary(dictionary, id)

                trackRecordings.add(trackRecording)
            } catch (exception: Exception) {
                // TODO
                Log.w("TrackService", "Could not construct track recording (Id=\"${dictionary.getString("_id")}\")!")
            }
        }

        return trackRecordings
    }

    public override fun getByIdOrNull(id: String): TrackRecording? {
        val result = this.couchDb.getDocument(id)
        if (result == null) {
            return null
        }

        return TrackRecording.fromCouchDbDocument(result)
    }

    public override fun save(item: TrackRecording) {
        val document = item.toCouchDbDocument()

        this.couchDb.save(document)
    }

    public override fun delete(id: String) {
        val result = this.couchDb.getDocument(id)

        this.couchDb.delete(result)
    }

    public override fun getAttachmentHandler(trackRecording: TrackRecording): IAttachmentHandler {
        val directoryNavigator = this.trackRecordingsDirectoryNavigator
                .getDirectory(trackRecording.id.toString())
                .getDirectory(TrackService.RECORDING_ATTACHMENTS_DIRECTORY_NAME)

        return FileSystemAttachmentHandler(trackRecording, directoryNavigator)
    }

    companion object {
        private const val RECORDING_ATTACHMENTS_DIRECTORY_NAME: String = "attachments"

        private const val RECORDING_BASE_DIRECTORY_NAME: String = "recordings"
    }
}