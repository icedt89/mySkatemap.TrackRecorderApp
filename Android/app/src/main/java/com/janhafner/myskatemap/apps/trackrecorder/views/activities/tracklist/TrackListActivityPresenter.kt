package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_track_list.*


internal final class TrackListActivityPresenter(private val view: TrackListActivity,
                                                private val trackService: ITrackService) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    init {
        this.view.setContentView(R.layout.activity_track_list)

        this.view.tracklistactivity_recorded_tracks_list.adapter = TrackListItemsAdapter()
    }

    public fun destroy() {

        this.sessionSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}