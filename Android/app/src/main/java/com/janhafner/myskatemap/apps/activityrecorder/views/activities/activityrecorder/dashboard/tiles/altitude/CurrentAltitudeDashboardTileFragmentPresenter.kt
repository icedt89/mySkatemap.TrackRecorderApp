package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.types.DashboardTileDisplayType
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import io.reactivex.Observable

internal final class CurrentAltitudeDashboardTileFragmentPresenter(private val context: Context,
                                                                   appSettings: IAppSettings,
                                                                   activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                   distanceConverterFactory: IDistanceConverterFactory)
    : AltitudeDashboardTileFragmentPresenter(appSettings, activityRecorderServiceController, distanceConverterFactory) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_currentaltitudedashboardtilefragmentpresenter_tile)
    }

    public override val supportedPresenterConnectorTypes: List<DashboardTileDisplayType> = listOf(DashboardTileDisplayType.TextOnly, DashboardTileDisplayType.LineChart)

    protected override fun getValueSourceObservable(trackRecorderSession: IActivitySession): Observable<Float> {
        return trackRecorderSession.locationsAggregation.altitude.latestValueChanged.map {
            it.toFloat()
        }
    }
}


