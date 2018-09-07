package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import android.util.Log
import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.executeUnitOfWork
import com.janhafner.myskatemap.apps.trackrecorder.services.ICouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import java.util.*

internal final class DashboardService(private val dashboardsCouchDbFactory: ICouchDbFactory) : ICrudRepository<Dashboard> {
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

                val dashboard = Dashboard.fromCouchDbDictionary(dictionary, id)

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

        return Dashboard.fromCouchDbDocument(result!!)
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