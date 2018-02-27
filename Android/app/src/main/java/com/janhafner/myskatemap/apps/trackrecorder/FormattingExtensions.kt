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

@Deprecated("Use ITrackDistanceFormatter instead!")
internal fun Float.formatTrackDistance(context: Context): String {
    if(this > 1000.0) {
        return context.getString(R.string.app_trackdistance_template_kilometers, (this / 1000.0f).roundTrackDistanceForDisplay(context))
    }

    return context.getString(R.string.app_trackdistance_template_meters, this.roundTrackDistanceForDisplay(context))
}

internal fun Float.roundTrackDistanceForDisplay(context: Context): String {
    val decimalFormat = DecimalFormat(context.getString(R.string.app_trackdistance_decimalformat))
    decimalFormat.roundingMode = RoundingMode.CEILING

    return decimalFormat.format(this)
}