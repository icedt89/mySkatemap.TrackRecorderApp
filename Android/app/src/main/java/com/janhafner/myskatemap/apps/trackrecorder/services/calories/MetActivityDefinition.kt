package com.janhafner.myskatemap.apps.trackrecorder.services.calories

// https://sites.google.com/site/compendiumofphysicalactivities/home
internal final class MetActivityDefinition(public val compendiumCode: String,
                                           public val metValue: Float) {
    companion object {
        public val generalBikingMetActivityCode = "01015"

        public val normalInlineSkatingMetActivityCode = "15591"
    }
}


