package com.janhafner.myskatemap.apps.trackrecorder.common.types

import java.util.*

public final class Dashboard constructor(id: UUID = UUID.randomUUID()) {
    public var id: UUID = id
        private set

    public var topLeftTileImplementationTypeName: String = "CurrentSpeedDashboardTileFragmentPresenter"

    public var topRightTileImplementationTypeName: String = "DistanceDashboardTileFragmentPresenter"

    public var middleCenterTileImplementationTypeName: String = "RecordingTimeDashboardTileFragmentPresenter"

    public var bottomLeftTileImplementationTypeName: String = "AverageSpeedDashboardTileFragmentPresenter"

    public var bottomRightTileImplementationTypeName: String = "BurnedEnergyDashboardTileFragmentPresenter"

}