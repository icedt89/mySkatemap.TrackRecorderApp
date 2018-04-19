package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import org.joda.time.DateTime

internal final class Attachment(public var displayName: String, public var filePath: String, public val attachedAt: DateTime) {
    public var comment : String? = null
}