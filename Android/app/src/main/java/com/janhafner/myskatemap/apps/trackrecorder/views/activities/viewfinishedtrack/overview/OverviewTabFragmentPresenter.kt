package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.overview

import android.graphics.Color
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.format
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_overview_tab.*


internal final class OverviewTabFragmentPresenter(private val view: OverviewTabFragment,
                                                  private val speedConverterFactory: ISpeedConverterFactory,
                                                  private val distanceConverterFactory: IDistanceConverterFactory,
                                                  private val appSettings: IAppSettings) {
    private var trackRecording: TrackRecording? = null

    init {
        this.setupTrack()
    }

    private fun setupTrack() {
        if(this.trackRecording == null) {
            return
        }

        val locationsObservable = Observable.fromArray(this.trackRecording!!.locations)
                .subscribeOn(Schedulers.computation())
                .share()

        val distanceConverter =  this.distanceConverterFactory.createConverter()
        val speedConverter = this.speedConverterFactory.createConverter()

        this.view.altitude_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        this.view.altitude_chart.axisRight.setDrawLabels(false)
        this.view.altitude_chart.axisLeft.valueFormatter = object : IAxisValueFormatter {
            public override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val result = distanceConverter.format(value)

                return result
            }
        }
        locationsObservable
                .map {
                    val data = mutableListOf<Entry>()
                    for (location in it.withIndex()) {
                        data.add(Entry(location.index.toFloat(), location.value.altitude!!.toFloat(), location.value))                    }

                    val dataSet = LineDataSet(data, "Altitude")
                    dataSet.setDrawCircles(false)

                    dataSet.color = Color.RED
                    LineData(dataSet)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.view.altitude_chart.data = it

                    this.view.altitude_chart.invalidate()
                }

        this.view.speed_chart.axisLeft.valueFormatter = object : IAxisValueFormatter {
            public override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val result = speedConverter.format(value)

                return result
            }
        }
        this.view.speed_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        this.view.speed_chart.axisRight.setDrawLabels(false)
        locationsObservable
                .map {
                    val data = mutableListOf<Entry>()
                    for (location in it.withIndex()) {
                        data.add(Entry(location.index.toFloat(), location.value.speed!!.toFloat(), location.value))

                    }

                    val dataSet = LineDataSet(data, "Speed")
                    dataSet.setDrawCircles(false)

                    dataSet.color = Color.RED
                    LineData(dataSet)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.view.speed_chart.data = it

                    this.view.speed_chart.invalidate()
                }

    }

    public fun setTrackRecording(trackRecording: TrackRecording) {
        this.trackRecording = trackRecording

        this.setupTrack()
    }
}