package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.hasChanged
import com.janhafner.myskatemap.apps.activityrecorder.core.isNamed
import com.janhafner.myskatemap.apps.activityrecorder.core.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.energy.getUnitSymbol
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal final class BurnedEnergyDashboardTileFragmentPresenter(private val context: Context,
                                                                private val appSettings: IAppSettings,
                                                                activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                private val energyConverterFactory: IEnergyConverterFactory)
    : DashboardTileFragmentPresenter(activityRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_burnedenergydashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        val value = 0.0f

        val result = this.energyConverterFactory.createConverter().convert(value)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: IActivitySession): Observable<FormattedDisplayValue> {
        return trackRecorderSession.burnedEnergyChanged
                .startWith(0.0f)
                .withLatestFrom(this.appSettings.propertyChanged
                        .hasChanged()
                        .isNamed(IAppSettings::energyConverterTypeName.name)
                        .map {
                            this.energyConverterFactory.createConverter()
                        }
                        .mergeWith(Observable.just(this.energyConverterFactory.createConverter())),
                        BiFunction<Float, IEnergyConverter, FormattedDisplayValue> {
                            value, converter ->
                            val result = converter.convert(value)

                            FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol(), value)
                        })
                .replay(1)
                .autoConnect()
    }
}