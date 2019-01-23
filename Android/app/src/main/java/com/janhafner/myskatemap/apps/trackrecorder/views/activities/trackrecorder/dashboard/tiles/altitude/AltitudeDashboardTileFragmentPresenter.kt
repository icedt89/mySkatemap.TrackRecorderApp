package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.janhafner.myskatemap.apps.trackrecorder.common.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.common.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.LineChartDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal abstract class AltitudeDashboardTileFragmentPresenter(private val appSettings: IAppSettings,
                                                               trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                               private val distanceConverterFactory: IDistanceConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    protected abstract fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float>

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        if (this.presenterConnector is LineChartDashboardTileFragmentPresenterConnector) {
            return Observable.empty()
        }

        val value = 0.0f

        val result = this.distanceConverterFactory.createConverter().convert(value)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
        return this.getValueSourceObservable(trackRecorderSession)
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

