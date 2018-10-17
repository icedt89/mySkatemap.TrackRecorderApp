package com.janhafner.myskatemap.apps.trackrecorder.common.types

import org.joda.time.Period
import java.util.*

public final class TrackInfo {
    public var id: UUID = UUID.randomUUID()

    public var displayName: String = ""

    public var distance: Float? = null

    public var recordingTime: Period = Period.ZERO
}