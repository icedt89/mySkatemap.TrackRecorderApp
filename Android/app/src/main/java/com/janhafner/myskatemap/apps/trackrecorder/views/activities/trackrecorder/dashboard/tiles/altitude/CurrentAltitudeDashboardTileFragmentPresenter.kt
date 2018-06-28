package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class CurrentAltitudeDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                   appSettings: IAppSettings,
                                                                   trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController) {
    public override fun initialize() {
        // TODO
        this.view.fragment_dashboard_tile_title.text = "Höhe"
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(
                trackRecorderSession.statistic.altitude.lastValueChanged
                        .map {
                            it.toString()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view.fragment_dashboard_tile_value.text())
        )

        return result
    }

    protected override fun resetView() {
        this.view.fragment_dashboard_tile_value.text = "0.0"
    }
}


