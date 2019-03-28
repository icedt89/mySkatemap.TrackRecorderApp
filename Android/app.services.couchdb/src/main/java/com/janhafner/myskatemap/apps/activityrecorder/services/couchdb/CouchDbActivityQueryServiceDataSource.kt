package com.janhafner.myskatemap.apps.activityrecorder.services.couchdb

import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.GetActivitiesQuery
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityQueryServiceDataSource
import io.reactivex.Single
import java.util.*

public final class CouchDbActivityQueryServiceDataSource(private val couchDbFactory: ICouchDbFactory) : IActivityQueryServiceDataSource {
    public override fun deleteActivityInfo(activityInfoId: String): Single<Unit> {
        return Single.fromCallable {
            this.couchDbFactory.executeUnitOfWork {
                val activityInfo = it.getDocument(activityInfoId)

                it.delete(activityInfo)
            }
        }
    }

    public override fun queryActivities(query: GetActivitiesQuery): Single<List<ActivityInfo>> {
        return Single.fromCallable {
            val activities = ArrayList<ActivityInfo>()

            this.couchDbFactory.executeUnitOfWork {
                val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                        .from(DataSource.database(it))
                        .where(Expression.property("documentType").`is`(Expression.string(ActivityInfo::class.java.simpleName)))
                        .orderBy(Ordering.property("startedAt").descending())

                val results = queryBuilder.execute()

                for (result in results) {
                    val id = UUID.fromString(result.getString("id"))
                    val dictionary = result.getDictionary(it.name)

                    val activity = ActivityInfoConverter.activityInfoFromCouchDbDictionary(dictionary, id)

                    activities.add(activity)
                }
            }

            activities as List<ActivityInfo>
        }
    }

    public override fun saveActivityInfo(activityInfo: ActivityInfo): Single<String> {
        return Single.fromCallable {
            var activityId = ""
            this.couchDbFactory.executeUnitOfWork {
                val activityInfoDocument = activityInfo.toCouchDbDocument()

                it.save(activityInfoDocument)

                activityId = activityInfoDocument.id
            }

            activityId
        }
    }
}