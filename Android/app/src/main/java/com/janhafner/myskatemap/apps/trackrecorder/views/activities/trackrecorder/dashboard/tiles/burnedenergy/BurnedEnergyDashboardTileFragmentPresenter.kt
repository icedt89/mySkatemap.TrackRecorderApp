package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.getUnitSymbol
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.disposables.Disposable

internal final class BurnedEnergyDashboardTileFragmentPresenter(private val context: Context,
                                                                private val appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                private val energyConverterFactory: IEnergyConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private var energyConverter: IEnergyConverter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.energyConverter = energyConverterFactory.createConverter()

        val title = this.context.getString(R.string.dashboard_tile_burnedenergydashboardtilefragmentpresenter_tile)

        this.titleChangedSubject.onNext(title)
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.burnedEnergyChanged
                .doOnNext{
                    this.currentValue = it
                }
                .mergeWith(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged && it.propertyName == IAppSettings::energyConverterTypeName.name
                        }
                        .doOnNext {
                            this.energyConverter = this.energyConverterFactory.createConverter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.energyConverter.convert(it)
                }
                .subscribe{
                    this.valueChangedSubject.onNext(it.value.roundWithTwoDecimals())
                    this.unitChangedSubject.onNext(it.unit.getUnitSymbol())
                })

        return result
    }

    protected override fun resetView() {
        val result = this.energyConverter.convert(this.defaultValue)

        this.valueChangedSubject.onNext(result.value.roundWithTwoDecimals())
        this.unitChangedSubject.onNext(result.unit.getUnitSymbol())
    }
}