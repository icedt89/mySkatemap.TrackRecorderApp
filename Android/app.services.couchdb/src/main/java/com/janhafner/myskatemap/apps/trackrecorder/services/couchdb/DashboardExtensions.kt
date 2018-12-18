package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import java.util.*

internal class DashboardConverter {
    public companion object {
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
    }
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
