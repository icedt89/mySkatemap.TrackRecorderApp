package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude

import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.hasChanged
import com.janhafner.myskatemap.apps.activityrecorder.core.isNamed
import com.janhafner.myskatemap.apps.activityrecorder.core.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.distance.getUnitSymbol
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.LineChartDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal abstract class AltitudeDashboardTileFragmentPresenter(private val appSettings: IAppSettings,
                                                               activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                               private val distanceConverterFactory: IDistanceConverterFactory)
    : DashboardTileFragmentPresenter(activityRecorderServiceController) {
    protected abstract fun getValueSourceObservable(trackRecorderSession: IActivitySession): Observable<Float>

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        if (this.presenterConnector is LineChartDashboardTileFragmentPresenterConnector) {
            return Observable.empty()
        }

        val value = 0.0f

        val result = this.distanceConverterFactory.createConverter().convert(value)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: IActivitySession): Observable<FormattedDisplayValue> {
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

