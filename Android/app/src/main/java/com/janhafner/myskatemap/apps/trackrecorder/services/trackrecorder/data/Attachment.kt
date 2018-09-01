package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data

import com.couchbase.lite.Dictionary
import com.couchbase.lite.MutableDictionary
import org.joda.time.DateTime

internal final class Attachment(public var displayName: String, public var filePath: String, public val attachedAt: DateTime) {
    public var comment : String? = null

    public fun toCouchDbDictionary() : Dictionary {
        val result = MutableDictionary()

        result.setDate("attachedAt", this.attachedAt.toDate())
        result.setString("comment", this.comment)
        result.setString("displayName", this.displayName)
        result.setString("filePath", this.filePath)

        return result
    }

    companion object {
        public fun fromCouchDbDictionary(dictionary : Dictionary) : Attachment {
            val displayName = dictionary.getString("displayName")
            val filePath = dictionary.getString("filePath")
            val attachedAt = DateTime(dictionary.getDate("attachedAt"))

            val result  = Attachment(displayName, filePath, attachedAt)
            result.comment = dictionary.getString("comment")

            return result
        }
    }
}