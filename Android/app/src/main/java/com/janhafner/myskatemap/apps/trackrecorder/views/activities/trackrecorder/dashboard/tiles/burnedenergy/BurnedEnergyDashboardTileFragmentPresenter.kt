package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.format
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class BurnedEnergyDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                private val energyConverterFactory: IEnergyConverterFactory)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {
    private var energyConverter: IEnergyConverter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.energyConverter = energyConverterFactory.createConverter()
    }

    public override fun initialize() {
        this.view.fragment_dashboard_tile_title.text = view.context!!.getString(R.string.dashboard_tile_burnedenergydashboardtilefragmentpresenter_tile)
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.burnedEnergyChanged
                .map {
                    it.kiloCalories
                }
                .doOnNext{
                    this.currentValue = it
                }
                .mergeWith(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged && it.propertyName == IAppSettings::energyUnitFormatterTypeName.name
                        }
                        .doOnNext {
                            this.energyConverter = this.energyConverterFactory.createConverter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.energyConverter.format(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.view.fragment_dashboard_tile_value.text()))

        return result
    }

    protected override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = this.energyConverter.format(this.defaultValue)
    }
}