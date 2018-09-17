package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable

internal final class AverageSpeedDashboardTileFragmentPresenter(private val context: Context,
                                                                appSettings: IAppSettings,
                                                                trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                speedConverterFactory: ISpeedConverterFactory)
    : SpeedDashboardTileFragmentPresenter(appSettings, trackRecorderServiceController, speedConverterFactory) {
    init {
        val title = this.context.getString(R.string.dashboard_tile_averagespeeddashboardtilefragmentpresenter_title)

        this.titleChangedSubject.onNext(title)
    }

    protected override fun getValueSourceObservable(trackRecorderSession: ITrackRecordingSession): Observable<Float> {
        return trackRecorderSession.locationsAggregation.speed.averageValueChanged
    }
}