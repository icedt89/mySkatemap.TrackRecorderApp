package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.TrackRecordingDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.common.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.ViewFinishedTrackActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.ViewFinishedTrackActivityPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_track_list.*
import kotlinx.android.synthetic.main.activity_track_list_item.view.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class TrackListActivityPresenter(private val view: TrackListActivity,
                                                private val trackQueryService: ITrackQueryService,
                                                private val distanceConverterFactory: IDistanceConverterFactory,
                                                private val appSettings: IAppSettings,
                                                private val trackService: ITrackService,
                                                private val notifier: INotifier) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var navigationDrawersOpened: Boolean = false

    init {
        this.view.setContentView(R.layout.activity_track_list)

        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_bright_24dp)

        val adapter = TrackListItemsAdapter()
        this.view.tracklistactivity_recorded_tracks_list.layoutManager = LinearLayoutManager(this.view)
        this.view.tracklistactivity_recorded_tracks_list.adapter = adapter

        this.subscriptions.addAll(
                adapter.itemViewCreated.subscribe {
                    val item = it

                    item.view.activity_track_list_item_displayname.text = item.item.displayName
                    item.view.activity_track_list_item_recordingtime.text = item.item.recordingTime.formatRecordingTime()

                    if(item.item.distance != null) {
                        val result = this.distanceConverterFactory.createConverter().format(it.item.distance!!)

                        item.view.activity_track_list_item_distance.text = result
                    }

                    if(item.item.isDeletable) {
                        item.view.delete_RENAMEME.clicks()
                                .flatMapSingle {
                                    this.trackService.deleteTrackRecordingById(item.item.trackInfo.id.toString())
                                            .subscribeOn(Schedulers.io())
                                }
                                .subscribe()
                    }

                    item.view.clicks()
                            .subscribe { result ->
                                val startViewFinishedTrackActivityIntent = Intent(this.view, ViewFinishedTrackActivity::class.java)

                                startViewFinishedTrackActivityIntent.putExtra(ViewFinishedTrackActivityPresenter.EXTRA_TRACK_RECORDING_KEY, item.item.trackInfo)

                                this.view.startActivity(startViewFinishedTrackActivityIntent)
                            }
                },
                this.notifier.notifications
                        .subscribeOn(Schedulers.computation())
                        .ofType(TrackRecordingDeletedEvent::class.java)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            val event = it

                            val items = adapter.findItems {
                                it.trackInfo.id.toString() == event.trackRecordingId
                            }

                            if(items.any()){
                                for (item in items) {
                                    adapter.remove(item)
                                }
                            }
                        }
        )


        this.trackQueryService.getTrackRecordings()
                .subscribeOn(Schedulers.io())
                .subscribe { items ->
                    val items = items.map {
                        TrackListItem(it)
                    }

                    adapter.addAll(items)
                }

        this.view.tracklistactivity_navigationdrawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            public override fun onDrawerStateChanged(newState: Int) {
            }

            public override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            public override fun onDrawerClosed(drawerView: View) {
                this@TrackListActivityPresenter.navigationDrawersOpened = false
            }

            public override fun onDrawerOpened(drawerView: View) {
                this@TrackListActivityPresenter.navigationDrawersOpened = true
            }
        })
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