package com.janhafner.myskatemap.apps.trackrecorder.services.userprofile

import com.couchbase.lite.Dictionary
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.Sex
import java.util.*

internal final class UserProfile {
    public constructor(id: UUID) {
        this.id = id
    }

    public var id: UUID = UUID.randomUUID()
        private set

    public var name: String = "Adam"

    public var age: Int? = null

    public var height: Float? = null

    public var weight: Float? = null

    public var sex: Sex? = null

    public fun toCouchDbDocument(): MutableDocument {
        val result = MutableDocument(this.id.toString())
        result.setString("documentType", this.javaClass.name)

        result.setString("name", this.name)
        result.setString("sex", this.sex.toString())

        if (this.age != null) {
            result.setInt("age", this.age!!)
        }

        if (this.height != null) {
            result.setFloat("height", this.height!!)
        }

        if (this.weight != null) {
            result.setFloat("weight", this.weight!!)
        }

        return result
    }

    companion object {
        public fun fromCouchDbDocument(document: Document) : UserProfile {
            val id = UUID.fromString(document.id)

            val result = UserProfile(id)

            result.sex = Sex.valueOf(document.getString("sex"))
            result.name = document.getString("name")

            result.age = document.getInt("age")
            result.height = document.getFloat("height")
            result.weight = document.getFloat("weight")

            return result
        }

        public fun fromCouchDbDictionary(dictionary: Dictionary, id: UUID) : UserProfile {
            val result = UserProfile(id)

            result.sex = Sex.valueOf(dictionary.getString("sex"))
            result.name = dictionary.getString("name")

            result.age = dictionary.getInt("age")
            result.height = dictionary.getFloat("height")
            result.weight = dictionary.getFloat("weight")

            return result
        }
    }
}