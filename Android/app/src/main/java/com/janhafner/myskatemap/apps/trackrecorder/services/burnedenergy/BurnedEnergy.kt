package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

internal final class BurnedEnergy(public val kiloCalories: Float) {
    public override fun toString(): String {
        return "BurnedEnergy[kcal:${this.kiloCalories}]"
    }

    companion object {
        public val empty: BurnedEnergy = BurnedEnergy(0.0f)
    }
}