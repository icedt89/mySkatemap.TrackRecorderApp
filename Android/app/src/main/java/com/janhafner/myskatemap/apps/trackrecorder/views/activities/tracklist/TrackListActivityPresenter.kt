package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_track_list.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class TrackListActivityPresenter(private val view: TrackListActivity,
                                                private val trackService: ITrackService) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.view.setContentView(R.layout.activity_track_list)

        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_bright_24dp)

        val adapter = TrackListItemsAdapter()
        this.view.tracklistactivity_recorded_tracks_list.layoutManager = LinearLayoutManager(this.view)
        this.view.tracklistactivity_recorded_tracks_list.adapter = adapter

        this.trackService.getTrackRecordings()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    items ->
                        val items = items.map {
                            val trackListItem = TrackListItem()
                            trackListItem.displayName = it.displayName
                            trackListItem.distance = it.distance
                            trackListItem.recordingTime = it.recordingTime
                            trackListItem.isDeletable = true

                            trackListItem
                        }

                        adapter.addAll(items)
                }
    }

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.view.onBackPressed()
        }

        return true
    }

    public fun destroy() {
        this.subscriptions.dispose()
    }
}