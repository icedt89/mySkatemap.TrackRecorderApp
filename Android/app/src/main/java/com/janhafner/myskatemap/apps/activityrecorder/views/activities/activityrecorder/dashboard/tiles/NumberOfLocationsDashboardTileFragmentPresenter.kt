package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.core.withCount
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import io.reactivex.Observable
import org.joda.time.DateTime

internal final class NumberOfLocationsDashboardTileFragmentPresenter(private val context: Context,
                                                                     activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(activityRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_numberoflocationsdashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        return Observable.just(FormattedDisplayValue("0", "#", 0))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: IActivitySession): Observable<FormattedDisplayValue> {
        return trackRecorderSession.locationsChanged
                .startWith(Location("", DateTime.now(), 0.0, 0.0))
                .withCount()
                .map {
                    FormattedDisplayValue(it.count.toString(), "#", it.count)
                }
                .replay(1)
                .autoConnect()
    }
}