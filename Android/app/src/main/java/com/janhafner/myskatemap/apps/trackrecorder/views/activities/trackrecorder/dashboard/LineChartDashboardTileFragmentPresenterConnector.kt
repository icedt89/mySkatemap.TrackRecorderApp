package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import android.view.View.GONE
import android.view.View.VISIBLE
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimalsAndFormatWithUnit
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal class LineChartDashboardTileFragmentPresenterConnector : IDashboardTileFragmentPresenterConnector {
    private var connectedLineChart: LineChart? = null

    public override fun connect(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>): List<Disposable> {
        if(this.connectedLineChart != null) {
            this.connectedLineChart!!.data?.clearValues()
            this.connectedLineChart!!.data = null
            this.connectedLineChart!!.clear()
        }

        val newLineChart = dashboardTileFragment.fragment_dashboard_tile_line_chart
        if (this.connectedLineChart != newLineChart) {
            dashboardTileFragment.fragment_dashboard_tile_unit.visibility = GONE
            dashboardTileFragment.fragment_dashboard_tile_value.visibility = GONE

            newLineChart.data?.clearValues()
            newLineChart.data = null
            newLineChart.clear()

            newLineChart.legend.isEnabled = false
            newLineChart.description.textColor = dashboardTileFragment.context!!.getColor(R.color.textColor)
            newLineChart.axisRight.isEnabled = false
            newLineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            newLineChart.xAxis.setDrawLabels(false)
            newLineChart.axisLeft.textColor = newLineChart.description.textColor
            newLineChart.axisLeft.setDrawLabels(false)
            newLineChart.visibility = VISIBLE

            this.connectedLineChart = newLineChart
        }

        return listOf(
                source
                    .subscribeOn(Schedulers.computation())
                    .filter {
                        this.connectedLineChart != null
                    }
                    .map {
                        object : Any() {
                            public val value = it

                            public val lineChart = this@LineChartDashboardTileFragmentPresenterConnector.connectedLineChart!!
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if(it.lineChart.data == null) {
                            val dataSet = FixedLineDataSet(mutableListOf(), "", 50)
                            dataSet.setDrawCircles(false)
                            dataSet.color = it.lineChart.context!!.getColor(R.color.accentColor)

                            val lineData = LineData(dataSet)

                            lineData.setDrawValues(false)

                            it.lineChart.data = lineData
                        }

                        val number = it.value.rawValue as Float
                        it.lineChart.description.text = number.roundWithTwoDecimalsAndFormatWithUnit(it.value.unit)

                        it.lineChart.data.addEntry(Entry(0.0f, number, it.value.value), 0)

                        it.lineChart.notifyDataSetChanged()
                        it.lineChart.invalidate()
                    }
        )
    }

    private final class FixedLineDataSet(yVals: List<Entry>, label: String, private val maximumNumberOfXValues: Int = Int.MAX_VALUE) : LineDataSet(yVals, label) {
        public override fun addEntry(e: Entry): Boolean {
            if(this.values.count() == maximumNumberOfXValues) {
                super.removeFirst()

                val result = super.addEntry(e)

                for (withIndex in this.values.withIndex()) {
                    withIndex.value.x = withIndex.index.toFloat()
                }

                return result
            }

            e.x = this.values.count().toFloat()

            return super.addEntry(e)
        }
    }
}