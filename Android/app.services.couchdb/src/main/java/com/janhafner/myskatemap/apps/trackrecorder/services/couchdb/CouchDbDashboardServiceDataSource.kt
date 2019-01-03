package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Document
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardServiceDataSource
import io.reactivex.Single

public final class CouchDbDashboardServiceDataSource(private val couchDbFactory: ICouchDbFactory) : IDashboardServiceDataSource {
    public override fun getDashboardByIdOrNull(id: String): Single<Optional<Dashboard>> {
        return Single.fromCallable {
            var result: Document? = null

            this.couchDbFactory.executeUnitOfWork {
                result = it.getDocument(id)
            }

            if (result == null) {
                Optional<Dashboard>(null)
            } else {
                val trackRecording = DashboardConverter.dashboardFromCouchDbDocument(result!!)

                Optional(trackRecording)
            }
        }
    }

    public override fun saveDashboard(dashboard: Dashboard): Single<String> {
        return Single.fromCallable {
            var dashboardId = ""
            this.couchDbFactory.executeUnitOfWork {
                val dashboardDocument = dashboard.toCouchDbDocument()

                it.save(dashboardDocument)

                dashboardId = dashboardDocument.id
            }

            dashboardId
        }
    }
}