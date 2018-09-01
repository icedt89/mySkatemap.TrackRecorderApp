package com.janhafner.myskatemap.apps.trackrecorder

import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.*
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

private val defaultTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendHourOfDay(2)
        .appendLiteral(":")
        .appendMinuteOfHour(2)
        .appendLiteral(":")
        .appendSecondOfMinute(2)
        .toFormatter()

private val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendDayOfMonth(2)
        .appendLiteral(".")
        .appendMonthOfYear(2)
        .appendLiteral(".")
        .appendYear(4, 4)
        .appendLiteral(" ")
        .appendHourOfDay(2)
        .appendLiteral(":")
        .appendMinuteOfHour(2)
        .appendLiteral(":")
        .appendSecondOfMinute(2)
        .toFormatter()

internal fun DateTime.formatDefault(): String {
    return defaultDateTimeFormatter.print(this)
}

internal fun DateTime.formatTimeOnlyDefault(): String {
    return defaultTimeFormatter.print(this)
}

internal fun Period.formatRecordingTime(): String {
    return trackRecordingTimeFormatter.print(this.normalizedStandard(PeriodType.time()))
}

internal fun Float.roundWithTwoDecimals(): String {
    val decimalFormat = DecimalFormat("#.##")
    decimalFormat.roundingMode = RoundingMode.CEILING

    return decimalFormat.format(this)
}

internal fun Float.formatDistanceKilometers() : String {
    return "${this.roundWithTwoDecimals()} km"
}

internal fun Float.formatDistanceMiles() : String {
    return "${this.roundWithTwoDecimals()} mi"
}

internal fun Float.formatDistanceMeters() : String {
    return "${this.roundWithTwoDecimals()} m"
}

internal fun Float.formatSpeedMetersPerSecond() : String {
    return "${this} m/s"
}

internal fun Float.formatSpeedKilometersPerHour() : String {
    return "${this.roundWithTwoDecimals()} km/h"
}

internal fun Float.formatSpeedMilesPerHour() : String {
    return "${this.roundWithTwoDecimals()} mi/h"
}

internal fun Float.formatBurnedEnergyKilocalorie() : String {
    return "${this} kcal"
}

internal fun Float.formatBurnedEnergyKilojoule() : String {
    return "${this} kJ"
}

internal fun Float.formatBurnedEnergyWattHour() : String {
    return "${this} wH"
}