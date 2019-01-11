package com.janhafner.myskatemap.apps.trackrecorder.common

internal final class BasalMetabolicFactorSet(public val factor1: Float,
                                             public val factor2: Float,
                                             public val factor3: Float,
                                             public val factor4: Float) {
    companion object {
        public val male: BasalMetabolicFactorSet = BasalMetabolicFactorSet(10.0f,
                6.25f,
                5.0f,
                5.0f)

        public val female: BasalMetabolicFactorSet = BasalMetabolicFactorSet(10.0f,
                6.25f,
                5.0f,
                161.0f)
    }
}