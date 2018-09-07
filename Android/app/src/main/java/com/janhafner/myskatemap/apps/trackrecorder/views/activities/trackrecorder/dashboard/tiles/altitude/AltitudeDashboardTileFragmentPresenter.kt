package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal abstract class AltitudeDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                            appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val distanceUnitFormatterFactory: IDistanceUnitFormatterFactory)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {

    private var distanceUnitFormatter: IDistanceUnitFormatter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(this.getValueSourceObservable(trackRecorderSession)
                .doOnNext {
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

    protected abstract fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float>

    override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = this.distanceUnitFormatter.format(this.defaultValue)
    }
}

