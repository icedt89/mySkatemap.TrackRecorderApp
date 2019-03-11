package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDictionary
import com.couchbase.lite.MutableDocument
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.core.types.DashboardTile
import com.janhafner.myskatemap.apps.trackrecorder.core.types.DashboardTileDisplayType
import java.util.*

internal class DashboardConverter {
    public companion object {
        internal fun dashboardFromCouchDbDocument(document: Document) : Dashboard {
            val id = UUID.fromString(document.id)

            val result = Dashboard(id)

            result.topLeftTile = dashboardTileFromCouchDbDictionary(document.getDictionary("topLeftTile"))
            result.topRightTile = dashboardTileFromCouchDbDictionary(document.getDictionary("topRightTile"))
            result.middleCenterTile = dashboardTileFromCouchDbDictionary(document.getDictionary("middleCenterTile"))
            result.bottomLeftTile = dashboardTileFromCouchDbDictionary(document.getDictionary("bottomLeftTile"))
            result.bottomRightTile = dashboardTileFromCouchDbDictionary(document.getDictionary("bottomRightTile"))

            return result
        }

        private fun dashboardTileFromCouchDbDictionary(dictionary: Dictionary) : DashboardTile {
            val result = DashboardTile()

            result.implementationTypeName = dictionary.getString("implementationTypeName")
            result.displayType = DashboardTileDisplayType.valueOf(dictionary.getString("displayType"))

            return result
        }
    }
}

internal fun Dashboard.toCouchDbDocument(): MutableDocument {
    val result = MutableDocument(this.id.toString())
    result.setString("documentType", this.javaClass.simpleName)

    result.setDictionary("topLeftTile", this.topLeftTile.toCouchDbDictionary())
    result.setDictionary("topRightTile", this.topRightTile.toCouchDbDictionary())
    result.setDictionary("middleCenterTile", this.middleCenterTile.toCouchDbDictionary())
    result.setDictionary("bottomLeftTile", this.bottomLeftTile.toCouchDbDictionary())
    result.setDictionary("bottomRightTile", this.bottomRightTile.toCouchDbDictionary())

    return result
}

internal fun DashboardTile.toCouchDbDictionary(): MutableDictionary {
    val result = com.couchbase.lite.MutableDictionary()

    result.setString("implementationTypeName", this.implementationTypeName)
    result.setString("displayType", this.displayType.toString())

    return result
}
