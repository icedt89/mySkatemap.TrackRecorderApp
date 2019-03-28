package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.types.DashboardTileDisplayType
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import io.reactivex.Observable

internal final class CurrentSpeedDashboardTileFragmentPresenter(private val context: Context,
                                                                appSettings: IAppSettings,
                                                                activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                speedConverterFactory: ISpeedConverterFactory)
    : SpeedDashboardTileFragmentPresenter(appSettings, activityRecorderServiceController, speedConverterFactory) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_currentspeeddashboardtilefragmentpresenter_tile)
    }

    public override val supportedPresenterConnectorTypes: List<DashboardTileDisplayType> = listOf(DashboardTileDisplayType.TextOnly, DashboardTileDisplayType.LineChart)

    protected override fun getValueSourceObservable(trackRecorderSession: IActivitySession): Observable<Float> {
        return trackRecorderSession.locationsAggregation.speed.latestValueChanged.map {
            it.toFloat()
        }
    }
}


