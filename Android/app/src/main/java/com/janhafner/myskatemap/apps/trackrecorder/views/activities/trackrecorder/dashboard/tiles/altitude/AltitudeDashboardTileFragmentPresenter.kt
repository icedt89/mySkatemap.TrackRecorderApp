package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
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
                                                            private val distanceConverterFactory: IDistanceConverterFactory)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {

    private var distanceConverter: IDistanceConverter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.distanceConverter = this.distanceConverterFactory.createConverter()
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
                            this.distanceConverter = this.distanceConverterFactory.createConverter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.distanceConverter.format(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.view.fragment_dashboard_tile_value.text()))

        return result
    }

    protected abstract fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float>

    override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = this.distanceConverter.format(this.defaultValue)
    }
}

