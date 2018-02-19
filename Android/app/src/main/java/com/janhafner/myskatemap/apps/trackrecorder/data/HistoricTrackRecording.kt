package com.janhafner.myskatemap.apps.trackrecorder.data

import org.joda.time.DateTime
import org.joda.time.Period

internal final class HistoricTrackRecording(public val numberOfLocations: Int,
                                            public val numberOfAttachments: Int,
                                            public val trackingStartedAt: DateTime,
                                            public val trackingFinishedAt: DateTime,
                                            public val recordingTime: Period,
                                            public val uploadedAt: DateTime) {
}