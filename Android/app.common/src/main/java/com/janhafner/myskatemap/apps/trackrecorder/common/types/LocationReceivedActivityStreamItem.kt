package com.janhafner.myskatemap.apps.trackrecorder.common.types

import org.joda.time.DateTime

public final class LocationReceivedActivityStreamItem(at: DateTime, public val latitude: Double, public val longitude: Double) : MapActivityStreamItem(at) {
}