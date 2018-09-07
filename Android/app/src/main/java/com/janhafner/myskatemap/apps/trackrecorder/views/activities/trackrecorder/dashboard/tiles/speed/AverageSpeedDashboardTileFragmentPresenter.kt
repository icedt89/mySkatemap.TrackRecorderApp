package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class AverageSpeedDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                speedUnitFormatterFactory: ISpeedUnitFormatterFactory)
    : SpeedDashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController, speedUnitFormatterFactory) {
    public override fun initialize() {
        this.view.fragment_dashboard_tile_title.text = view.context!!.getString(R.string.dashboard_tile_averagespeeddashboardtilefragmentpresenter_title)
    }

    protected override fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float> {
        return trackRecorderSession.statistic.speed.averageValueChanged
    }
}