package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityType

internal final class MetCodeActivityTypeMapping(public val code: String, public val activityType: ActivityType) {
    public companion object {
        private val activityTypeMappings: Map<String, ActivityType> = mapOf(
                Pair("01015", ActivityType.OnBicycle),
                Pair("15591", ActivityType.Running),
                Pair("17160", ActivityType.Walking)
        )

        public fun getActivityTypeByCode(code: String) : ActivityType? {
            // 1015 -> 01015
            val fixedCode = code.padStart(5, '0')

            if (!this.activityTypeMappings.containsKey(fixedCode)) {
                return null
            }

            return this.activityTypeMappings[fixedCode]!!
        }
    }
}