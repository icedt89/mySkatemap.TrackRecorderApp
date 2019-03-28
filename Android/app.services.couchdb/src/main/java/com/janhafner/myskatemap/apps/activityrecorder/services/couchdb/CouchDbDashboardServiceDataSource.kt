package com.janhafner.myskatemap.apps.activityrecorder.services.couchdb

import com.janhafner.myskatemap.apps.activityrecorder.core.Optional
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Dashboard
import com.janhafner.myskatemap.apps.activityrecorder.services.dashboard.IDashboardServiceDataSource
import io.reactivex.Single

public final class CouchDbDashboardServiceDataSource(private val couchDbFactory: ICouchDbFactory) : IDashboardServiceDataSource {
    public override fun getDashboardByIdOrNull(id: String): Single<Optional<Dashboard>> {
        return Single.fromCallable {
            var result: Dashboard? = null

            this.couchDbFactory.executeUnitOfWork {
                val document = it.getDocument(id)

                if(document != null) {
                    result = DashboardConverter.dashboardFromCouchDbDocument(document)
                }
            }

            Optional(result)
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