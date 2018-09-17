package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable

internal final class AverageAltitudeDashboardTileFragmentPresenter(private val context: Context,
                                                                   appSettings: IAppSettings,
                                                                   trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                   distanceConverterFactory: IDistanceConverterFactory)
    : AltitudeDashboardTileFragmentPresenter(appSettings, trackRecorderServiceController, distanceConverterFactory) {
    init {
        val title = this.context.getString(R.string.dashboard_tile_averagealtitudedashboardtilefragmentpresenter_title)

        this.titleChangedSubject.onNext(title)
    }

    protected override fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float> {
        return trackRecorderSession.locationsAggregation.altitude.averageValueChanged
    }
}