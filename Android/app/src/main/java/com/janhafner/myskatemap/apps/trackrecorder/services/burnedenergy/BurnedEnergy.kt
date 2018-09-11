package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

internal final class BurnedEnergy(public val kiloCalories: Float) {
    companion object {
        public val empty: BurnedEnergy = BurnedEnergy(0.0f)
    }
}