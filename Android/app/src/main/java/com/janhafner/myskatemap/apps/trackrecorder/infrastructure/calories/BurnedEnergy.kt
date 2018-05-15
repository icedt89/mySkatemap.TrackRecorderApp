package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories

internal final class BurnedEnergy(public val kiloCalories: Float) {
    public val kiloJoule: Float

    public val wattHour: Float

    init {
        this.kiloJoule = this.kiloCalories * BurnedEnergy.KiloCaloriesToKiloJouleFactor
        this.wattHour = this.kiloCalories * BurnedEnergy.KiloCaloriesToWattHourFactor
    }

    public override fun toString(): String {
        return "BurnedEnergy[kcal:${this.kiloCalories};kj:${this.kiloJoule};wattH:${this.wattHour}]"
    }

    companion object {
        private const val KiloCaloriesToKiloJouleFactor: Float = 4.1868f

        private const val KiloCaloriesToWattHourFactor: Float = 1.163f
    }
}