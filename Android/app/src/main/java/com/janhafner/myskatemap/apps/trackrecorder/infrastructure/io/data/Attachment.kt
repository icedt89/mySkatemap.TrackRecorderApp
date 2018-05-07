package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import org.joda.time.DateTime
import java.util.*

internal final class Attachment(public var displayName: String, public var filePath: String, public val attachedAt: DateTime) {
    public val id: UUID = UUID.randomUUID()

    public var comment : String? = null
}