package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.common.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

internal final class BurnedEnergyDashboardTileFragmentPresenter(private val context: Context,
                                                                private val appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                private val energyConverterFactory: IEnergyConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private var energyConverter: IEnergyConverter

    init {
        this.energyConverter = energyConverterFactory.createConverter()

        this.title = this.context.getString(R.string.dashboard_tile_burnedenergydashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        val result = this.energyConverter.convert(0.0f)

        return Observable.just(FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol()))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
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

                            FormattedDisplayValue(result.value.roundWithTwoDecimals(), result.unit.getUnitSymbol())
                        })
                .replay(1)
                .autoConnect()
    }
}