package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data

import com.couchbase.lite.Dictionary
import com.couchbase.lite.MutableDictionary
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.Sex

internal final class FitnessActivity(public val age: Int,
                                     public val metActivityCode: String,
                                     public val weightInKilograms: Float,
                                     public val heightInCentimeters: Int,
                                     public val sex: Sex) {
    public fun toCouchDbDictionary() : Dictionary {
        val result = MutableDictionary()

        result.setInt("age", this.age)
        result.setString("metActivityCode", this.metActivityCode)
        result.setFloat("weightInKilograms", this.weightInKilograms)
        result.setInt("heightInCentimeters", this.heightInCentimeters)
        result.setString("sex", this.sex.toString())

        return result
    }

    companion object {
        public fun fromCouchDbDictionary(dictionary : Dictionary) : FitnessActivity {
            val age = dictionary.getInt("age")
            val metActivityCode = dictionary.getString("metActivityCode")
            val weightInKilograms = dictionary.getFloat("weightInKilograms")
            val heightInCentimeters = dictionary.getInt("heightInCentimeters")
            val sex = Sex.valueOf(dictionary.getString("sex"))

            return FitnessActivity(age, metActivityCode, weightInKilograms, heightInCentimeters, sex)
        }
    }
}