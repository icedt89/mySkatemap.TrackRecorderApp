package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.core.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.core.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal final class DistanceDashboardTileFragmentPresenter(private val context: Context,
                                                            private val appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val distanceConverterFactory: IDistanceConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_distancedashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        val value = 0.0f
        val result = this.distanceConverterFactory.createConverter().convert(value)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
        return trackRecorderSession.distanceChanged
                .startWith(0.0f)
                .withLatestFrom(this.appSettings.propertyChanged
                        .hasChanged()
                        .isNamed(IAppSettings::distanceConverterTypeName.name)
                        .map {
                            this.distanceConverterFactory.createConverter()
                        }
                        .mergeWith(Observable.just(this.distanceConverterFactory.createConverter())),
                        BiFunction<Float, IDistanceConverter, FormattedDisplayValue> {
                            value, converter ->
                            val result = converter.convert(value)

                            FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value)
                        })
                .replay(1)
                .autoConnect()
    }
}


