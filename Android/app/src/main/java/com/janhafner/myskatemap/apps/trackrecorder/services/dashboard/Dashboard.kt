package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy.BurnedEnergyDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance.DistanceDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragment
import java.util.*

internal final class Dashboard {
    public constructor(id: UUID) {
        this.id = id
    }

    public var id: UUID = UUID.randomUUID()
        private set

    public var topLeftTileImplementationTypeName: String = DistanceDashboardTileFragment::class.java.name

    public var topRightTileImplementationTypeName: String = BurnedEnergyDashboardTileFragment::class.java.name

    public var middleCenterTileImplementationTypeName: String = CurrentSpeedDashboardTileFragment::class.java.name

    public var bottomLeftTileImplementationTypeName: String = AverageSpeedDashboardTileFragment::class.java.name

    public var bottomRightTileImplementationTypeName: String = CurrentAltitudeDashboardTileFragment::class.java.name

    public fun toCouchDbDocument(): MutableDocument {
        val result = MutableDocument(this.id.toString())
        result.setString("documentType", this.javaClass.name)

        result.setString("topLeftTileImplementationTypeName", this.topLeftTileImplementationTypeName)
        result.setString("topRightTileImplementationTypeName", this.topRightTileImplementationTypeName)
        result.setString("middleCenterTileImplementationTypeName", this.middleCenterTileImplementationTypeName)
        result.setString("bottomLeftTileImplementationTypeName", this.bottomLeftTileImplementationTypeName)
        result.setString("bottomRightTileImplementationTypeName", this.bottomRightTileImplementationTypeName)

        return result
    }

    companion object {
        public fun fromCouchDbDocument(document: Document) : Dashboard {
            val id = UUID.fromString(document.id)

            val result = Dashboard(id)

            result.topLeftTileImplementationTypeName = document.getString("topLeftTileImplementationTypeName")
            result.topRightTileImplementationTypeName = document.getString("topRightTileImplementationTypeName")
            result.middleCenterTileImplementationTypeName = document.getString("middleCenterTileImplementationTypeName")
            result.bottomLeftTileImplementationTypeName = document.getString("bottomLeftTileImplementationTypeName")
            result.bottomRightTileImplementationTypeName = document.getString("bottomRightTileImplementationTypeName")

            return result
        }

        public fun fromCouchDbDictionary(dictionary: Dictionary, id: UUID) : Dashboard {
            val result = Dashboard(id)

            result.topLeftTileImplementationTypeName = dictionary.getString("topLeftTileImplementationTypeName")
            result.topRightTileImplementationTypeName = dictionary.getString("topRightTileImplementationTypeName")
            result.middleCenterTileImplementationTypeName = dictionary.getString("middleCenterTileImplementationTypeName")
            result.bottomLeftTileImplementationTypeName = dictionary.getString("bottomLeftTileImplementationTypeName")
            result.bottomRightTileImplementationTypeName = dictionary.getString("bottomRightTileImplementationTypeName")

            return result
        }
    }
}