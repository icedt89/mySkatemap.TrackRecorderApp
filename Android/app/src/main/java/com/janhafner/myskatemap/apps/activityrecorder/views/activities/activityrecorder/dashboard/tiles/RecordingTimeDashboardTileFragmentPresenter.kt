package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.formatRecordingTime
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import io.reactivex.Observable
import org.joda.time.Period

internal final class RecordingTimeDashboardTileFragmentPresenter(private val context: Context,
                                                                 activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(activityRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_recordingtimedashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        val value = Period.ZERO

        return Observable.just(FormattedDisplayValue(value.formatRecordingTime(), "", value))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: IActivitySession): Observable<FormattedDisplayValue> {
        return trackRecorderSession.recordingTimeChanged
                .startWith(Period.ZERO)
                .map {
                    val formattedValue = it.formatRecordingTime()

                    FormattedDisplayValue(formattedValue, "", it)
                }
                .replay(1)
                .autoConnect()
    }
}