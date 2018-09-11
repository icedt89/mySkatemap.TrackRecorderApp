package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import java.util.*

internal fun dashboardFromCouchDbDocument(document: Document) : Dashboard {
    val id = UUID.fromString(document.id)

    val result = Dashboard(id)

    result.topLeftTileImplementationTypeName = document.getString("topLeftTileImplementationTypeName")
    result.topRightTileImplementationTypeName = document.getString("topRightTileImplementationTypeName")
    result.middleCenterTileImplementationTypeName = document.getString("middleCenterTileImplementationTypeName")
    result.bottomLeftTileImplementationTypeName = document.getString("bottomLeftTileImplementationTypeName")
    result.bottomRightTileImplementationTypeName = document.getString("bottomRightTileImplementationTypeName")

    return result
}

internal fun dashboardFromCouchDbDictionary(dictionary: Dictionary, id: UUID) : Dashboard {
    val result = Dashboard(id)

    result.topLeftTileImplementationTypeName = dictionary.getString("topLeftTileImplementationTypeName")
    result.topRightTileImplementationTypeName = dictionary.getString("topRightTileImplementationTypeName")
    result.middleCenterTileImplementationTypeName = dictionary.getString("middleCenterTileImplementationTypeName")
    result.bottomLeftTileImplementationTypeName = dictionary.getString("bottomLeftTileImplementationTypeName")
    result.bottomRightTileImplementationTypeName = dictionary.getString("bottomRightTileImplementationTypeName")

    return result
}

internal fun Dashboard.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setString("topLeftTileImplementationTypeName", this.topLeftTileImplementationTypeName)
    result.setString("topRightTileImplementationTypeName", this.topRightTileImplementationTypeName)
    result.setString("middleCenterTileImplementationTypeName", this.middleCenterTileImplementationTypeName)
    result.setString("bottomLeftTileImplementationTypeName", this.bottomLeftTileImplementationTypeName)
    result.setString("bottomRightTileImplementationTypeName", this.bottomRightTileImplementationTypeName)

    return result
}