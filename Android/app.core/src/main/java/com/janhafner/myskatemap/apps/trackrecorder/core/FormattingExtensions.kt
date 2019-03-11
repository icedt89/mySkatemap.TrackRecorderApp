package com.janhafner.myskatemap.apps.trackrecorder.core

import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder
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

public fun DateTime.formatDefault(): String {
    return defaultDateTimeFormatter.print(this)
}

public fun DateTime.formatTimeOnlyDefault(): String {
    return defaultTimeFormatter.print(this)
}

public fun Period.formatRecordingTime(): String {
    return trackRecordingTimeFormatter.print(this.normalizedStandard(PeriodType.time()))
}

public fun Float.roundWithTwoDecimals(): String {
    return this.toDouble().roundWithTwoDecimals()
}

public fun Double.roundWithTwoDecimals(): String {
    val decimalFormat = DecimalFormat("#.##")
    decimalFormat.roundingMode = RoundingMode.CEILING

    return decimalFormat.format(this)
}

public fun Float.roundWithTwoDecimalsAndFormatWithUnit(unitSymbol: String) : String {
    return this.toDouble().roundWithTwoDecimalsAndFormatWithUnit(unitSymbol)
}

public fun Double.roundWithTwoDecimalsAndFormatWithUnit(unitSymbol: String) : String {
    return "${this.roundWithTwoDecimals()} ${unitSymbol}"
}