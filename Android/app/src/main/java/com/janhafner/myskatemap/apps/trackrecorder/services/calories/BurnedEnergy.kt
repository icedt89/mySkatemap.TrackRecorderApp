package com.janhafner.myskatemap.apps.trackrecorder.services.calories

internal final class BurnedEnergy(public val kiloCalories: Float) {
    public val kiloJoule: Float

    public val wattHour: Float

    init {
        this.kiloJoule = this.kiloCalories * BurnedEnergy.KILO_CALORIES_TO_JOULE_CONVERSION_FACTOR
        this.wattHour = this.kiloCalories * BurnedEnergy.KILO_CALORIES_TO_WATTHOUR_CONVERSION_FACTOR
    }

    public override fun toString(): String {
        return "BurnedEnergy[kcal:${this.kiloCalories};kj:${this.kiloJoule};wattH:${this.wattHour}]"
    }

    companion object {
        private const val KILO_CALORIES_TO_JOULE_CONVERSION_FACTOR: Float = 4.1868f

        private const val KILO_CALORIES_TO_WATTHOUR_CONVERSION_FACTOR: Float = 1.163f

        public val empty: BurnedEnergy = BurnedEnergy(0.0f)
    }
}