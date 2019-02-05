package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed

import com.janhafner.myskatemap.apps.trackrecorder.common.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.common.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.LineChartDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.FormattedDisplayValue
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal abstract class SpeedDashboardTileFragmentPresenter(private val appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val speedConverterFactory: ISpeedConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        if (this.presenterConnector is LineChartDashboardTileFragmentPresenterConnector) {
            return Observable.empty()
        }

        val value = 0.0f

        val result = this.speedConverterFactory.createConverter().convert(value)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
        return this.getValueSourceObservable(trackRecorderSession)
                .startWith(0.0f)
                .withLatestFrom(this.appSettings.propertyChanged
                        .hasChanged()
                        .isNamed(IAppSettings::speedConverterTypeName.name)
                        .map {
                            this.speedConverterFactory.createConverter()
                        }
                        .mergeWith(Observable.just(this.speedConverterFactory.createConverter())),
                        BiFunction<Float, ISpeedConverter, FormattedDisplayValue> {
                            value, converter ->
                            val result = converter.convert(value)

                            FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), result.value)
                        })
                .replay(1)
                .autoConnect()
    }



    protected abstract fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float>
}