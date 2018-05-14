package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories

// https://sites.google.com/site/compendiumofphysicalactivities/home
internal final class MetabolicEquivalentActivityDefinition(public val compendiumCode: String,
                                                           public val metabolicEquivalent: Float) {
    // TODO: Load activities from metdefinitions.json dynamically by compendium code!
    companion object {
        public val generalBiking = MetabolicEquivalentActivityDefinition("01015", 7.5f)

        public val normalInlineSkating = MetabolicEquivalentActivityDefinition("15591", 7.5f)
    }
}