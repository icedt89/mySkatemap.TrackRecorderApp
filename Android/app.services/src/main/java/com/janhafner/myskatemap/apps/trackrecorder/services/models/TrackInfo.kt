package com.janhafner.myskatemap.apps.trackrecorder.services.models

import org.joda.time.DateTime
import java.util.*

public final class TrackInfo {
    public var id: UUID = UUID.randomUUID()

    public var displayName: String = ""

    public var trackingStartedAt: DateTime = DateTime.now()

    public var trackingFinishedAt: DateTime = DateTime.now()
}