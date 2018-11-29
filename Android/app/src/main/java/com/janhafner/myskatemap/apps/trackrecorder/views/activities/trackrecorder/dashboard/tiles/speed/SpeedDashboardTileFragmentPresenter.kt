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
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal abstract class SpeedDashboardTileFragmentPresenter(private val appSettings: IAppSettings,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                            private val speedConverterFactory: ISpeedConverterFactory)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private var speedConverter: ISpeedConverter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.speedConverter = this.speedConverterFactory.createConverter()
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(this.getValueSourceObservable(trackRecorderSession)
                .doOnNext {
                    this.currentValue = it
                }
                .mergeWith(this.appSettings.propertyChanged
                        .hasChanged()
                        .isNamed(IAppSettings::speedConverterTypeName.name)
                        .doOnNext {
                            this.speedConverter = this.speedConverterFactory.createConverter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.speedConverter.convert(it)
                }
                .subscribe{
                    this.valueChangedSubject.onNext(it.value.roundWithTwoDecimals())
                    this.unitChangedSubject.onNext(it.unit.getUnitSymbol())
                })

        return result
    }

    protected abstract fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float>

    override fun resetView() {
        val result = this.speedConverter.convert(this.defaultValue)

        this.valueChangedSubject.onNext(result.value.roundWithTwoDecimals())
        this.unitChangedSubject.onNext(result.unit.getUnitSymbol())
    }
}

