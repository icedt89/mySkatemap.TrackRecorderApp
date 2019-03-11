package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.core.withCount
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable
import org.joda.time.DateTime

internal final class NumberOfLocationsDashboardTileFragmentPresenter(private val context: Context,
                                                                     trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_numberoflocationsdashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        return Observable.just(FormattedDisplayValue("0", "#", 0))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
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