package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable
import org.joda.time.Period

internal final class RecordingTimeDashboardTileFragmentPresenter(private val context: Context,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    init {
        this.title = this.context.getString(R.string.dashboard_tile_recordingtimedashboardtilefragmentpresenter_tile)
    }

    protected override fun getResetObservable(): Observable<FormattedDisplayValue> {
        return Observable.just(FormattedDisplayValue(Period.ZERO.formatRecordingTime(), ""))
    }

    protected override fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue> {
        return trackRecorderSession.recordingTimeChanged
                .startWith(Period.ZERO)
                .map {
                    val formattedValue = it.formatRecordingTime()

                    FormattedDisplayValue(formattedValue, "")
                }
                .replay(1)
                .autoConnect()
    }
}