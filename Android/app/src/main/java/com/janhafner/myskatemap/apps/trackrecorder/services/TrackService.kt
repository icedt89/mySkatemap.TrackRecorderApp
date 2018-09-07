package com.janhafner.myskatemap.apps.trackrecorder.services

import android.util.Log
import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.executeUnitOfWork
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import java.util.*

internal final class TrackService(private val trackRecordingsCouchDbFactory: ICouchDbFactory,
                                  private val appBaseDirectoryNavigator: IDirectoryNavigator,
                                  private val appSettings: IAppSettings) : ITrackService {
    private val trackRecordingsDirectoryNavigator: IDirectoryNavigator

    init {
        this.trackRecordingsDirectoryNavigator = this.appBaseDirectoryNavigator.getDirectory(TrackService.RECORDING_BASE_DIRECTORY_NAME)
    }

    public override fun getAll(): List<TrackRecording> {
        val trackRecordings = ArrayList<TrackRecording>()

        this.trackRecordingsCouchDbFactory.executeUnitOfWork {
            val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                    .from(DataSource.database(it))
                    .where(Expression.property("_id").isNot(Expression.value(this.appSettings.currentTrackRecordingId?.toString()))
                            .and(Expression.property("documentType").`is`(Expression.string(TrackRecording::class.java.simpleName))))

            val results = queryBuilder.execute()

            for (result in results) {
                val id = UUID.fromString(result.getString("id"))
                val dictionary = result.getDictionary(it.name)

                val trackRecording = TrackRecording.fromCouchDbDictionary(dictionary, id)

                trackRecordings.add(trackRecording)
            }
        }

        return trackRecordings
    }

    public override fun getByIdOrNull(id: String): TrackRecording? {
        var result: Document? = null

        this.trackRecordingsCouchDbFactory.executeUnitOfWork {
            result = it.getDocument(id)
        }

        if (result == null) {
            return null
        }

        return TrackRecording.fromCouchDbDocument(result!!)
    }

    public override fun save(item: TrackRecording) {
        this.trackRecordingsCouchDbFactory.executeUnitOfWork {
            val document = item.toCouchDbDocument()

            it.save(document)
        }
    }

    public override fun delete(id: String) {
        this.trackRecordingsCouchDbFactory.executeUnitOfWork {
            val result = it.getDocument(id)

            it.delete(result)
        }
    }

    companion object {
        private const val RECORDING_ATTACHMENTS_DIRECTORY_NAME: String = "attachments"

        private const val RECORDING_BASE_DIRECTORY_NAME: String = "recordings"
    }
}