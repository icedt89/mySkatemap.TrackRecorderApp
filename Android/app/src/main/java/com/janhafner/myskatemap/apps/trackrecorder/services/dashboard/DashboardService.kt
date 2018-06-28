package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import android.util.Log
import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import java.util.*

internal final class DashboardService(private val couchDb: Database) : ICrudRepository<Dashboard> {
    public override fun getAll(): List<Dashboard> {
        val queryBuilder = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(couchDb))
                .where(Expression.property("documentType").`is`(Expression.string(Dashboard::javaClass.name)))

        val results = queryBuilder.execute()

        val dashboards = ArrayList<Dashboard>()

        for (result in results) {
            val id = UUID.fromString(result.getString("id"))
            val dictionary = result.getDictionary(couchDb.name)

            try {
                val dashboard = Dashboard.fromCouchDbDictionary(dictionary, id)

                dashboards.add(dashboard)
            } catch (exception: Exception) {
                // TODO
                Log.w("UserProfileService", "Could not construct dashboard configuration (Id=\"${dictionary.getString("_id")}\")!")
            }
        }

        return dashboards
    }

    public override fun getByIdOrNull(id: String): Dashboard? {
        val result = this.couchDb.getDocument(id)
        if (result == null) {
            return null
        }

        return Dashboard.fromCouchDbDocument(result)
    }

    public override fun save(item: Dashboard) {
        val document = item.toCouchDbDocument()

        this.couchDb.save(document)
    }

    public override fun delete(id: String) {
        val result = this.couchDb.getDocument(id)

        this.couchDb.delete(result)
    }
}