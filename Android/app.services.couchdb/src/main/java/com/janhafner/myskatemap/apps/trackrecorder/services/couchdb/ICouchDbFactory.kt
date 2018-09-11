package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Database

public interface ICouchDbFactory {
    fun createDatabase() : Database
}