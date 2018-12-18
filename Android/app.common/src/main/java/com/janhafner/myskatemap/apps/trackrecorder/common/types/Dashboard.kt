package com.janhafner.myskatemap.apps.trackrecorder.common.types

import java.util.*

public final class Dashboard constructor(id: UUID = UUID.randomUUID()) {
    public var id: UUID = id
        private set

    public lateinit var topLeftTileImplementationTypeName: String

    public lateinit var topRightTileImplementationTypeName: String

    public lateinit var middleCenterTileImplementationTypeName: String

    public lateinit var bottomLeftTileImplementationTypeName: String

    public lateinit var bottomRightTileImplementationTypeName: String

}