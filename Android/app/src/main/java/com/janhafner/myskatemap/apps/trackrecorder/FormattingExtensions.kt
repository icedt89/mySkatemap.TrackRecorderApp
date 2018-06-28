package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import java.math.RoundingMode
import java.text.DecimalFormat

private val trackRecordingTimeFormatter: PeriodFormatter = PeriodFormatterBuilder()
        .minimumPrintedDigits(2)
        .printZeroAlways()
        .appendHours()
        .appendSeparator(":")
        .appendMinutes()
        .appendSeparator(":")
        .appendSeconds()
        .toFormatter()

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()

internal fun DateTime.formatDefault(): String {
    return dateTimeFormatter.print(this)
}

internal fun Period.formatRecordingTime(): String {
    return trackRecordingTimeFormatter.print(this.normalizedStandard(PeriodType.time()))
}

internal fun Float.roundTrackDistanceForDisplay(context: Context): String {
    val decimalFormat = DecimalFormat(context.getString(R.string.app_trackdistance_decimalformat))
    decimalFormat.roundingMode = RoundingMode.CEILING

    return decimalFormat.format(this)
}

internal fun Float.formatSpeed() : String {
    return "${this} km/h"
}

internal fun BurnedEnergy.formatKilocalorie() : String {
    return "${this.kiloCalories} kcal"
}

internal fun BurnedEnergy.formatKilojoule() : String {
    return "${this.kiloJoule} kJ"
}

internal fun BurnedEnergy.formatWattHour() : String {
    return "${this.wattHour} wH"
}

internal fun Temperature.formatKelvin() : String {
    return "${this.kelvin} K"
}

internal fun Temperature.formatCelsius() : String {
    return "${this.celsius} °C"
}

internal fun Temperature.formatFahrenheit() : String {
    return "${this.fahrenheit} °F"
}