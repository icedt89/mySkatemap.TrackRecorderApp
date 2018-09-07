package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
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

internal final class MinimumAltitudeDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                   appSettings: IAppSettings,
                                                                   trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                   distanceUnitFormatterFactory: IDistanceUnitFormatterFactory)
    : AltitudeDashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController, distanceUnitFormatterFactory) {
    public override fun initialize() {
        this.view.fragment_dashboard_tile_title.text = view.context!!.getString(R.string.dashboard_tile_minimumaltitudedashboardtilefragmentpresenter_tile)
    }

    protected override fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float> {
        return trackRecorderSession.statistic.altitude.minimumValueChanged
    }
}