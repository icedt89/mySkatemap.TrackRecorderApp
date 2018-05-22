package com.janhafner.myskatemap.apps.trackrecorder.services

import android.util.Log
import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class CouchDbTrackService(private val couchDb: Database,
                                         private val appBaseDirectoryNavigator: IDirectoryNavigator,
                                         private val appSettings: IAppSettings) : ITrackService {
    private val trackRecordingsDirectoryNavigator: IDirectoryNavigator

    init {
        this.trackRecordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(CouchDbTrackService.RecordingBasedirectoryName)
    }

    public override fun getAllTrackRecordings(): List<TrackRecording> {
        val queryBuilder = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(couchDb))
                .where(Expression.property("_id").isNot(Expression.value(this.appSettings.currentTrackRecordingId?.toString())))

        val results = queryBuilder.execute()

        val trackRecordings = ArrayList<TrackRecording>()

        for (result in results) {
            val document = result.getDictionary(couchDb.name)

            try {
                val trackRecording = TrackRecording.fromCouchDbDictionary(document)

                trackRecordings.add(trackRecording)
            } catch (exception: Exception) {
                // TODO
                Log.w("CouchDbTrackService", "Could not construct track recording (Id=\"${document.getString("id")}\")!")
            }
        }

        return trackRecordings
    }

    public override fun getTrackRecording(id: String): TrackRecording? {
        val result = this.couchDb.getDocument(id)

        return TrackRecording.fromCouchDbDocument(result)
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording) {
        val document = trackRecording.toCouchDbDocument()

        this.couchDb.save(document)
    }

    public override fun deleteTrackRecording(id: String) {
        val result = this.couchDb.getDocument(id)

        this.couchDb.delete(result)
    }

    public override fun hasTrackRecording(id: String): Boolean {
        return this.couchDb.getDocument(id) != null
    }

    public override fun getAttachmentHandler(trackRecording: TrackRecording): IAttachmentHandler {
        val directoryNavigator = this.trackRecordingsDirectoryNavigator
                .getDirectory(trackRecording.id.toString())
                .getDirectory(CouchDbTrackService.RecordingAttachmentsDirectoryName)

        return FileSystemAttachmentHandler(trackRecording, directoryNavigator)
    }

    companion object {
        private const val RecordingAttachmentsDirectoryName: String = "attachments"

        private const val RecordingBasedirectoryName: String = "recordings"
    }
}