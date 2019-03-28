package com.janhafner.myskatemap.apps.activityrecorder.services.couchdb

import com.couchbase.lite.Database

public interface ICouchDbFactory {
    fun createDatabase() : Database
}