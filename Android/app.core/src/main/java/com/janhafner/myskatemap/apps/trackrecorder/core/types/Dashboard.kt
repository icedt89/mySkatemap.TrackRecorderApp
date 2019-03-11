package com.janhafner.myskatemap.apps.trackrecorder.core.types

import java.util.*

public final class Dashboard constructor(id: UUID = UUID.randomUUID()) {
    public var id: UUID = id
        private set

    public var topLeftTile = DashboardTile()

    public var topRightTile = DashboardTile()

    public var middleCenterTile = DashboardTile()

    public var bottomLeftTile = DashboardTile()

    public var bottomRightTile = DashboardTile()
}

