package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.overview

import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
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

    private fun setupTrack() {
        val distanceConverter = this.distanceConverterFactory.createConverter()
        val speedConverter = this.speedConverterFactory.createConverter()

        val source = Observable.fromArray(this.trackRecording!!.locations)
                .subscribeOn(Schedulers.computation())
                .filterNotEmpty()
                .replay()
                .autoConnect()

        val accuracyCorridorColor = Color.argb(100, 0, 145, 234)

        this.view.altitude_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        this.view.altitude_chart.xAxis.textColor = this.view.context!!.getColor(R.color.textColor)
        this.view.altitude_chart.axisRight.setDrawLabels(false)
        this.view.altitude_chart.axisLeft.textColor = this.view.altitude_chart.xAxis.textColor
        this.view.altitude_chart.legend.textColor = this.view.altitude_chart.xAxis.textColor
        this.view.altitude_chart.xAxis.setDrawLabels(false)
        this.view.altitude_chart.description.isEnabled = false
        this.view.altitude_chart.axisLeft.valueFormatter = object : IAxisValueFormatter {
            public override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return distanceConverter.format(value)
            }
        }
        source.map {
            val dataMin = mutableListOf<Entry>()
            val data = mutableListOf<Entry>()
            val dataMax = mutableListOf<Entry>()

            for (location in it.withIndex()) {
                val index = location.index.toFloat()
                val altitude = location.value.altitude!!.toFloat()
                val verticalAccuracyMeters = location.value.verticalAccuracyMeters ?: 0.0f

                Log.i("V-ACCURACY", verticalAccuracyMeters.toString())

                dataMin.add(Entry(index, altitude - Math.min(verticalAccuracyMeters, altitude), location.value))
                data.add(Entry(index, altitude, location.value))
                dataMax.add(Entry(index, altitude + verticalAccuracyMeters, location.value))
            }

            val datasetMin = LineDataSet(dataMin, "Min. Altitude")
            datasetMin.setDrawCircles(false)
            datasetMin.color = accuracyCorridorColor
            datasetMin.valueTextColor = datasetMin.color

            val dataset = LineDataSet(data, "Altitude")
            dataset.setDrawCircles(false)
            dataset.color = this.view.context!!.getColor(R.color.accentColor)
            dataset.valueTextColor = dataset.color

            val datasetMax = LineDataSet(dataMax, "Max. Altitude")
            datasetMax.setDrawCircles(false)
            datasetMax.color = datasetMin.color
            datasetMax.valueTextColor = datasetMax.color

            LineData(datasetMin, dataset, datasetMax)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.view.altitude_chart.data = it

                    this.view.altitude_chart.invalidate()
                }

        this.view.speed_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        this.view.speed_chart.xAxis.textColor = this.view.context!!.getColor(R.color.textColor)
        this.view.speed_chart.axisRight.setDrawLabels(false)
        this.view.speed_chart.description.isEnabled = false
        this.view.speed_chart.axisLeft.textColor = this.view.speed_chart.xAxis.textColor
        this.view.speed_chart.legend.textColor = this.view.speed_chart.xAxis.textColor
        this.view.speed_chart.xAxis.setDrawLabels(false)
        this.view.speed_chart.axisLeft.valueFormatter = object : IAxisValueFormatter {
            public override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return speedConverter.format(value)
            }
        }
        source.map {
            val dataMin = mutableListOf<Entry>()
            val data = mutableListOf<Entry>()
            val dataMax = mutableListOf<Entry>()

            for (location in it.withIndex()) {
                val index = location.index.toFloat()
                val speed = location.value.speed!!.toFloat()
                val speedAccuracyMetersPerSecond = location.value.speedAccuracyMetersPerSecond
                        ?: 0.0f

                Log.i("S-ACCURACY", speedAccuracyMetersPerSecond.toString())

                dataMin.add(Entry(index, speed - Math.min(speedAccuracyMetersPerSecond, speed), location.value))
                data.add(Entry(index, speed, location.value))
                dataMax.add(Entry(index, speed + speedAccuracyMetersPerSecond, location.value))
            }

            val datasetMin = LineDataSet(dataMin, "Min. Speed")
            datasetMin.setDrawCircles(false)
            datasetMin.color = accuracyCorridorColor
            datasetMin.valueTextColor = datasetMin.color

            val dataset = LineDataSet(data, "Speed")
            dataset.setDrawCircles(false)
            dataset.color = this.view.context!!.getColor(R.color.accentColor)
            dataset.valueTextColor = dataset.color

            val datasetMax = LineDataSet(dataMax, "Max. Speed")
            datasetMax.setDrawCircles(false)
            datasetMax.color = datasetMin.color
            datasetMax.valueTextColor = datasetMax.color

            LineData(datasetMin, dataset, datasetMax)
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