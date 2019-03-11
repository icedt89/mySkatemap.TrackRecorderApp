package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.track.GetTracksQuery
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryServiceDataSource
import io.reactivex.Single
import java.util.*

public final class CouchDbTrackQueryServiceDataSource(private val couchDbFactory: ICouchDbFactory) : ITrackQueryServiceDataSource {
    public override fun deleteTrackInfo(trackInfoId: String): Single<Unit> {
        return Single.fromCallable {
            this.couchDbFactory.executeUnitOfWork {
                val trackInfo = it.getDocument(trackInfoId)

                it.delete(trackInfo)
            }
        }
    }

    public override fun queryTrackRecordings(query: GetTracksQuery): Single<List<TrackInfo>> {
        return Single.fromCallable {
            val trackRecordings = ArrayList<TrackInfo>()

            this.couchDbFactory.executeUnitOfWork {
                val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                        .from(DataSource.database(it))
                        .where(Expression.property("documentType").`is`(Expression.string(TrackInfo::class.java.simpleName)))
                        .orderBy(Ordering.property("startedAt").descending())

                val results = queryBuilder.execute()

                for (result in results) {
                    val id = UUID.fromString(result.getString("id"))
                    val dictionary = result.getDictionary(it.name)

                    val trackRecording = TrackInfoConverter.trackInfoFromCouchDbDictionary(dictionary, id)

                    trackRecordings.add(trackRecording)
                }
            }

            trackRecordings as List<TrackInfo>
        }
    }

    public override fun saveTrackInfo(trackInfo: TrackInfo): Single<String> {
        return Single.fromCallable {
            var trackRecordingId = ""
            this.couchDbFactory.executeUnitOfWork {
                val trackInfoDocument = trackInfo.toCouchDbDocument()

                it.save(trackInfoDocument)

                trackRecordingId = trackInfoDocument.id
            }

            trackRecordingId
        }
    }
}