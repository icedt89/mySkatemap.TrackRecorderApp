package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.disposables.Disposable
import org.joda.time.Period

internal final class RecordingTimeDashboardTileFragmentPresenter(private val context: Context,
                                                            trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private val defaultValue: Period = Period.ZERO

    init {
        this.title = this.context.getString(R.string.dashboard_tile_recordingtimedashboardtilefragmentpresenter_tile)
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.recordingTimeChanged
                .map {
                    it.formatRecordingTime()
                }
                .subscribe{
                    this.valueChangedSubject.onNext(it)
                })

        return result
    }

    protected override fun resetView() {
        this.valueChangedSubject.onNext(this.defaultValue.formatRecordingTime())
        this.unitChangedSubject.onNext("")
    }
}