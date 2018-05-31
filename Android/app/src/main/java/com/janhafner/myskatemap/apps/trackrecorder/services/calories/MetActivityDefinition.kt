package com.janhafner.myskatemap.apps.trackrecorder.services.calories

// https://sites.google.com/site/compendiumofphysicalactivities/home
internal final class MetActivityDefinition(public val compendiumCode: String,
                                           public val metValue: Float) {
    companion object {
        public const val GENERAL_BIKING_MET_ACTIVITY_CODE = "01015"

        public const val NORMAL_INLINE_SKATING_MET_ACTIVITY_CODE = "15591"
    }
}


