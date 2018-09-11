package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import java.util.*

public final class CouchDbDashboardService(private val dashboardsCouchDbFactory: ICouchDbFactory) : ICrudRepository<Dashboard> {
    public override fun getAll(): List<Dashboard> {
        val dashboards = ArrayList<Dashboard>()

        this.dashboardsCouchDbFactory.executeUnitOfWork {

            val queryBuilder = QueryBuilder.select(SelectResult.all())
                    .from(DataSource.database(it))
                    .where(Expression.property("documentType").`is`(Expression.string(Dashboard::class.java.simpleName)))

            val results = queryBuilder.execute()

            for (result in results) {
                val id = UUID.fromString(result.getString("id"))
                val dictionary = result.getDictionary(it.name)

                val dashboard = dashboardFromCouchDbDictionary(dictionary, id)

                dashboards.add(dashboard)
            }
        }

        return dashboards
    }

    public override fun getByIdOrNull(id: String): Dashboard? {
        var result: Document? = null

        this.dashboardsCouchDbFactory.executeUnitOfWork {
            result = it.getDocument(id)
        }

        if(result == null) {
            return null
        }

        return dashboardFromCouchDbDocument(result!!)
    }

    public override fun save(item: Dashboard) {
        this.dashboardsCouchDbFactory.executeUnitOfWork {
            val document = item.toCouchDbDocument()

            it.save(document)
        }
    }

    public override fun delete(id: String) {
        this.dashboardsCouchDbFactory.executeUnitOfWork {
            val result = it.getDocument(id)

            it.delete(result)
        }
    }
}