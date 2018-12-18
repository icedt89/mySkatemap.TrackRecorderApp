package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import com.janhafner.myskatemap.apps.trackrecorder.common.withCount
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.disposables.Disposable

internal final class NumberOfLocationsDashboardTileFragmentPresenter(trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>)
    : DashboardTileFragmentPresenter(trackRecorderServiceController) {
    private val defaultValue: Int= 0

    init {
        this.title = "Locations"

        this.unitChangedSubject.onNext("#")
    }

    protected override fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable> {
        val result = ArrayList<Disposable>()

        result.add(trackRecorderSession.locationsChanged
                .withCount()
                .map {
                    it.count
                }
                .subscribe{
                    this.valueChangedSubject.onNext(it.toString())
                })

        return result
    }

    protected override fun resetView() {
        this.valueChangedSubject.onNext(this.defaultValue.toString())
    }
}