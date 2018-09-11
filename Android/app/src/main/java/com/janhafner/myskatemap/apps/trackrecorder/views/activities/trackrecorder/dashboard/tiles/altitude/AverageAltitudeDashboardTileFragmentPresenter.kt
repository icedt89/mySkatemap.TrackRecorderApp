package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal final class AverageAltitudeDashboardTileFragmentPresenter(view: DashboardTileFragment,
                                                                   appSettings: IAppSettings,
                                                                   trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                   distanceConverterFactory: IDistanceConverterFactory)
    : AltitudeDashboardTileFragmentPresenter(view, appSettings, trackRecorderServiceController, distanceConverterFactory) {
    public override fun initialize() {
        this.view.fragment_dashboard_tile_title.text = view.context!!.getString(R.string.dashboard_tile_averagealtitudedashboardtilefragmentpresenter_title)
    }

    protected override fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float> {
        return trackRecorderSession.locationsAggregation.altitude.averageValueChanged
    }
}