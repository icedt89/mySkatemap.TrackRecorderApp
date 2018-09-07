package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class DistanceDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                            appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val distanceUnitFormatterFactory: IDistanceUnitFormatterFactory)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {
    private var distanceUnitFormatter: IDistanceUnitFormatter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.distanceUnitFormatter = distanceUnitFormatterFactory.createFormatter()
    }

    public override fun initialize() {
        this.view.fragment_dashboard_tile_title.text = view.context!!.getString(R.string.dashboard_tile_distancedashboardtilefragmentpresenter_tile)
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.distanceChanged
                .doOnNext{
                    this.currentValue = it
                }
                .mergeWith(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged && it.propertyName == IAppSettings::distanceUnitFormatterTypeName.name
                        }
                        .doOnNext {
                            this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.distanceUnitFormatter.format(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.view.fragment_dashboard_tile_value.text()))

        return result
    }

    protected override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = this.distanceUnitFormatter.format(this.defaultValue)
    }
}