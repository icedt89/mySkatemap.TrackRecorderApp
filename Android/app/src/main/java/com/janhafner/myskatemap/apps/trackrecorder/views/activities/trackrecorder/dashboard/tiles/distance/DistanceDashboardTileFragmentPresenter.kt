package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.disposables.Disposable

internal final class DistanceDashboardTileFragmentPresenter(private val context: Context,
                                                            private val appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val distanceConverterFactory: IDistanceConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private var distanceConverter: IDistanceConverter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.distanceConverter = distanceConverterFactory.createConverter()

        val title = this.context.getString(R.string.dashboard_tile_distancedashboardtilefragmentpresenter_tile)

        this.titleChangedSubject.onNext(title)
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.distanceChanged
                .doOnNext{
                    this.currentValue = it
                }
                .mergeWith(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged && it.propertyName == IAppSettings::distanceConverterTypeName.name
                        }
                        .doOnNext {
                            this.distanceConverter = this.distanceConverterFactory.createConverter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.distanceConverter.convert(it)
                }
                .subscribe{
                    this.valueChangedSubject.onNext(it.value.roundWithTwoDecimals())
                    this.unitChangedSubject.onNext(it.unit.getUnitSymbol())
                })

        return result
    }

    protected override fun resetView() {
        val result = this.distanceConverter.convert(this.defaultValue)

        this.valueChangedSubject.onNext(result.value.roundWithTwoDecimals())
        this.unitChangedSubject.onNext(result.unit.getUnitSymbol())
    }
}