package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.track.GetTracksQuery
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryServiceDataSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

public final class CouchDbTrackQueryServiceDataSource(private val couchDbFactory: ICouchDbFactory) : ITrackQueryServiceDataSource {
    public override fun queryTrackRecordings(query: GetTracksQuery): Single<List<TrackInfo>> {
        return Single.fromCallable {
            val trackRecordings = ArrayList<TrackInfo>()

            this.couchDbFactory.executeUnitOfWork {
                val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                        .from(DataSource.database(it))
                        .where(Expression.property("documentType").`is`(Expression.string(TrackInfo::class.java.simpleName)))

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
        .subscribeOn(Schedulers.io())
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
        .subscribeOn(Schedulers.io())
    }
}