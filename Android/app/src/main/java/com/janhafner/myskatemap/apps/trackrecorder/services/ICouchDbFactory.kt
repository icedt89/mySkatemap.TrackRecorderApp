package com.janhafner.myskatemap.apps.trackrecorder.services

import com.couchbase.lite.Database

internal interface ICouchDbFactory {
    fun createDatabase() : Database
}