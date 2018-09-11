package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Database

public fun ICouchDbFactory.executeUnitOfWork(action: (database: Database) -> Unit) {
    var database: Database? = null
    try {
        database = this.createDatabase()

        action(database)
    } finally {
        database?.close()
    }
}