package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration

public final class CouchDbFactory(private val databaseName: String, private val databaseConfiguration: DatabaseConfiguration) : ICouchDbFactory {
    public override fun createDatabase(): Database {
        return Database(databaseName, databaseConfiguration)
    }
}