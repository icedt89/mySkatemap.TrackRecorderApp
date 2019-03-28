package com.janhafner.myskatemap.apps.activityrecorder.live

import org.joda.time.DateTime

public final class LiveLocation(public val capturedAt: DateTime,
                                public val latitude: Double,
                                public val longitude: Double) {
}

