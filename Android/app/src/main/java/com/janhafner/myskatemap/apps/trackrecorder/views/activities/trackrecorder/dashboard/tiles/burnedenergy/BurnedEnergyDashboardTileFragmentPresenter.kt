package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class BurnedEnergyDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                private val energyUnitFormatterFactory: IEnergyUnitFormatterFactory)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {
    private var energyUnitFormatter: IEnergyUnitFormatter

    private var currentValue: Float = 0.0f

    private val defaultValue: Float= 0.0f

    init {
        this.energyUnitFormatter = energyUnitFormatterFactory.createFormatter()
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
                            this.energyUnitFormatter = this.energyUnitFormatterFactory.createFormatter()
                        }
                        .map {
                            this.currentValue
                        })
                .map {
                    this.energyUnitFormatter.format(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.view.fragment_dashboard_tile_value.text()))

        return result
    }

    protected override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = this.energyUnitFormatter.format(this.defaultValue)
    }
}