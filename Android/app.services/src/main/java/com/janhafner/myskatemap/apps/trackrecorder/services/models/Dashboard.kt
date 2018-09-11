package com.janhafner.myskatemap.apps.trackrecorder.services.models

import java.util.*

public final class Dashboard {
    public constructor(id: UUID) {
        this.id = id
    }

    public var id: UUID = UUID.randomUUID()
        private set

    public var topLeftTileImplementationTypeName: String = "DistanceDashboardTileFragment"

    public var topRightTileImplementationTypeName: String = "BurnedEnergyDashboardTileFragment"

    public var middleCenterTileImplementationTypeName: String = "CurrentSpeedDashboardTileFragment"

    public var bottomLeftTileImplementationTypeName: String = "AverageSpeedDashboardTileFragment"

    public var bottomRightTileImplementationTypeName: String = "CurrentAltitudeDashboardTileFragment"

}