package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.content.Intent
import android.view.View
import android.view.View.GONE
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackRecordingDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.getActivityDrawableResourceId
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.ActivityWithAppNavigationPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.ViewFinishedTrackActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.ViewFinishedTrackActivityPresenter.Companion.EXTRA_TRACK_RECORDING_KEY
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_track_list.*
import kotlinx.android.synthetic.main.activity_track_list_item.view.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class TrackListActivityPresenter(view: TrackListActivity,
                                                private val trackQueryService: ITrackQueryService,
                                                private val distanceConverterFactory: IDistanceConverterFactory,
                                                private val appSettings: IAppSettings,
                                                private val trackService: ITrackService,
                                                private val notifier: INotifier): ActivityWithAppNavigationPresenter<TrackListActivity>(view, R.layout.activity_track_list) {
    private val adapter: TrackListItemsAdapter

    init {
        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_bright_24dp)

        this.adapter = TrackListItemsAdapter()
        this.view.tracklistactivity_recorded_tracks_list.layoutManager = LinearLayoutManager(this.view)
        this.view.tracklistactivity_recorded_tracks_list.adapter = this.adapter

        adapter.arrayChanged
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    this.adapter.itemCount == 0
                }
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe(this.view.tracklistactivity_no_activities_recorded.visibility())
        adapter.itemViewCreated
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    val item = it

                    item.view.activity_track_list_item_displayname.text = item.item.displayName
                    item.view.activity_track_list_item_recordingtime.text = item.item.recordingTime.formatRecordingTime()

                    val result = this.distanceConverterFactory.createConverter().format(it.item.distance)
                    item.view.activity_track_list_item_distance.text = result

                    var activityDrawableResId = item.view.context.getActivityDrawableResourceId(item.item.activityCode)
                    if(activityDrawableResId == null) {
                        activityDrawableResId = R.drawable.ic_directions_run_bright_24dp
                    }

                    item.view.activity_track_list_item_activity_icon.setImageResource(activityDrawableResId)

                    item.view.activity_track_list_item_action_delete.clicks()
                            .flatMapSingle {
                                this.trackService.deleteTrackRecordingById(item.item.trackInfo.id.toString())
                                        .subscribeOn(Schedulers.io())
                            }
                            .subscribe()

                    item.view.clicks()
                            .subscribe { result ->
                                val startViewFinishedTrackActivityIntent = Intent(this.view, ViewFinishedTrackActivity::class.java)

                                startViewFinishedTrackActivityIntent.putExtra(EXTRA_TRACK_RECORDING_KEY, item.item.trackInfo)

                                this.view.startActivity(startViewFinishedTrackActivityIntent)
                            }
                }
        this.notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(TrackRecordingDeletedEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose {
                    this.app_navigationdrawer.removeDrawerListener(this)
                }
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    val event = it

                    val items = adapter.findItems {
                        it.trackInfo.id.toString() == event.trackRecordingId
                    }

                    if (items.any()) {
                        for (item in items) {
                            adapter.remove(item)
                        }
                    }
                }

        this.loadActivities()
    }

    private fun loadActivities() {
        this.view.tracklistactivity_no_activities_recorded.visibility = GONE
        this.view.tracklistactivity_loading_activities.show()

        this.trackQueryService.getTrackRecordings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    if(items.any()) {
                        val items = items.map {
                            TrackListItem(it)
                        }

                        this.adapter.addAll(items)
                    } else {
                        this.view.tracklistactivity_no_activities_recorded.visibility = View.VISIBLE
                    }

                    this.view.tracklistactivity_loading_activities.hide()
                }
    }
}