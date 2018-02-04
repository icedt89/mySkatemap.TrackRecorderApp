package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Period
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
    return trackRecordingTimeFormatter.print(this)
}

internal fun Float.formatTrackDistance(context: Context): String {
    if(this > 1000.0) {
        return context.getString(R.string.app_trackdistance_template_kilometer, (this / 1000.0f).roundToTwoDecimalPlaces(context))
    }

    return context.getString(R.string.app_trackdistance_template_meter, this.roundToTwoDecimalPlaces(context))
}

internal fun Float.roundToTwoDecimalPlaces(context: Context): String {
    val decimalFormat = DecimalFormat(context.getString(R.string.app_trackdistance_decimalformat))
    decimalFormat.roundingMode = RoundingMode.CEILING

    return decimalFormat.format(this)
}