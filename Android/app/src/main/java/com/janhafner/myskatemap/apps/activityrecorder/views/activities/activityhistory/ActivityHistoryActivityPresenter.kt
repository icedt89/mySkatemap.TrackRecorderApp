package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityhistory

import android.content.Intent
import android.view.View
import android.view.View.GONE
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivityDeletedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.core.formatRecordingTime
import com.janhafner.myskatemap.apps.activityrecorder.getActivityDrawableResourceId
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityQueryService
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityService
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.ActivityWithAppNavigationPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.ViewFinishedActivityActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.ViewFinishedActivityActivityPresenter.Companion.EXTRA_ACTIVITY_KEY
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activityhistory_activity.*
import kotlinx.android.synthetic.main.activityhistory_listitem.view.*
import kotlinx.android.synthetic.main.app_navigation.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class ActivityHistoryActivityPresenter(view: ActivityHistoryActivity,
                                                      private val activityQueryService: IActivityQueryService,
                                                      private val distanceConverterFactory: IDistanceConverterFactory,
                                                      private val appSettings: IAppSettings,
                                                      private val activityService: IActivityService,
                                                      private val notifier: INotifier): ActivityWithAppNavigationPresenter<ActivityHistoryActivity>(view, R.layout.activityhistory_activity) {
    private val adapter: ActivityHistoryItemsAdapter

    init {
        this.view.setSupportActionBar(this.view.app_toolbar)

        this.view.app_navigation_action_activityhistory.setTextColor(this.view.getColor(R.color.appBlue))

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)

        this.adapter = ActivityHistoryItemsAdapter()
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
                        activityDrawableResId = R.drawable.ic_directions_run_24dp
                    }

                    item.view.activity_track_list_item_activity_icon.setImageResource(activityDrawableResId)

                    item.view.activity_track_list_item_action_delete.clicks()
                            .flatMapSingle {
                                this.activityService.deleteActivityById(item.item.activityInfo.id.toString())
                                        .subscribeOn(Schedulers.io())
                            }
                            .subscribe()

                    item.view.clicks()
                            .subscribe { result ->
                                val startViewFinishedTrackActivityIntent = Intent(this.view, ViewFinishedActivityActivity::class.java)

                                startViewFinishedTrackActivityIntent.putExtra(EXTRA_ACTIVITY_KEY, item.item.activityInfo)

                                this.view.startActivity(startViewFinishedTrackActivityIntent)
                            }
                }
        this.notifier.notifications
                .subscribeOn(Schedulers.computation())
                .ofType(ActivityDeletedEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose {
                    this.app_navigationdrawer.removeDrawerListener(this)
                }
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    val event = it

                    val items = adapter.findItems {
                        it.activityInfo.id.toString() == event.activityId
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

        this.activityQueryService.getActivities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    if(items.any()) {
                        val items = items.map {
                            ActivityHistoryItem(it)
                        }

                        this.adapter.addAll(items)
                    } else {
                        this.view.tracklistactivity_no_activities_recorded.visibility = View.VISIBLE
                    }

                    this.view.tracklistactivity_loading_activities.hide()
                }
    }
}