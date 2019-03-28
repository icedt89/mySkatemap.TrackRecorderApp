package com.janhafner.myskatemap.apps.activityrecorder.services.couchdb

import com.janhafner.myskatemap.apps.activityrecorder.core.Optional
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityServiceDataSource
import io.reactivex.Single

public final class CouchDbActivityServiceDataSource(private val couchDbFactory: ICouchDbFactory) : IActivityServiceDataSource {
    public override fun getActivityByIdOrNull(id: String): io.reactivex.Single<Optional<Activity>> {
        return Single.fromCallable {
            var result: Activity? = null

            this.couchDbFactory.executeUnitOfWork {
                val document = it.getDocument(id)

                if(document != null) {
                    result = ActivityConverter.activityFromCouchDbDocument(document)
                }
            }

            Optional(result)
        }
    }

    public override fun saveActivity(activity: Activity): io.reactivex.Single<String> {
        return Single.fromCallable {
            var activityId = ""
            this.couchDbFactory.executeUnitOfWork {
                val activityDocument = activity.toCouchDbDocument()

                it.save(activityDocument)

                activityId = activityDocument.id
            }

            activityId
        }
    }

    public override fun deleteActivityById(id: String): io.reactivex.Single<Unit> {
        return Single.fromCallable {
            this.couchDbFactory.executeUnitOfWork {
                val result = it.getDocument(id)

                it.delete(result)
            }
        }
    }
}