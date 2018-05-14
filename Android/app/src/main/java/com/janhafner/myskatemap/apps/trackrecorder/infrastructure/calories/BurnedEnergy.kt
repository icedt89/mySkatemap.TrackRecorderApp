package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories

internal final class BurnedEnergy(public val kiloCalories: Double) {
    public val kiloJoule: Double

    public val wattHour: Double

    init {
        this.kiloJoule = this.kiloCalories * 4.1868
        this.wattHour = this.kiloCalories * 1.163
    }

    public override fun toString(): String {
        return "BurnedEnergy[kcal:${this.kiloCalories}; kj:${this.kiloJoule}; wattH:${this.wattHour}]"
    }
}