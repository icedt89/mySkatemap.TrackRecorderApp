package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.content.Intent
import android.support.design.widget.NavigationView
import android.view.MenuItem
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.checkAllAppPermissions
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings.SettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_track_list.*
import kotlinx.android.synthetic.main.activity_track_list_item.view.*

internal final class TrackListActivityPresenter(private val trackListActivity: TrackListActivity,
                                                private val trackService: ITrackService,
                                                private val appSettings: IAppSettings,
                                                private val trackDistanceCalculator: TrackDistanceCalculator,
                                                private val distanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter = distanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

    init {
        this.trackListActivity.setContentView(R.layout.activity_track_list)

        this.trackListActivity.setSupportActionBar(this.trackListActivity.tracklistactivity_toolbar)

        this.trackListActivity.checkAllAppPermissions().subscribe { areAllGranted ->
            if(areAllGranted) {
                this.forwardToTrackRecorderIfNecessary()
            }else {
                this.trackListActivity.finishAndRemoveTask()
            }
        }
    }

    private fun forwardToTrackRecorderIfNecessary() {
        val hasCurrentTrackRecording: Boolean
        if(this.appSettings.currentTrackRecordingId != null) {
            hasCurrentTrackRecording = this.trackService.hasTrackRecording(this.appSettings.currentTrackRecordingId!!.toString())
            if(!hasCurrentTrackRecording) {
                this.appSettings.currentTrackRecordingId = null
            }
        } else {
            hasCurrentTrackRecording = false
        }

        if(hasCurrentTrackRecording) {
            val intent = Intent(this.trackListActivity, TrackRecorderActivity::class.java)
            intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.TryResume.toString())

            this.trackListActivity.startActivity(intent)

            this.trackListActivity.finish()
        } else {
            this.setupActivity()
        }
    }

    private fun setupActivity() {
        val items = this.trackService.getAllTrackRecordings()

        val itemsObservable = Observable.fromArray(items)

        val itemsAdapter = TrackListItemsAdapter(this.trackListActivity)

        this.subscriptions.addAll(
                itemsAdapter.itemViewCreated.subscribe{
                    trackDistanceCalculator.addAll(it.item.locations.values.toList())
                    it.view.activity_track_list_item_tracking_distance.text = this.trackDistanceUnitFormatter.format(trackDistanceCalculator.distance)
                    trackDistanceCalculator.clear()

                    it.view.activity_track_list_item_tracking_name.text = it.item.name
                    it.view.activity_track_list_item_tracking_duration.text = it.item.recordingTime.formatRecordingTime()
                    it.view.activity_track_list_item_tracking_startedat.text = it.item.trackingStartedAt.formatDefault()

                    if(it.item.isFinished) {
                        it.view.activity_track_list_item_tracking_finishedat.text = it.item.trackingFinishedAt!!.formatDefault()
                    }
                },

                appSettings.appSettingsChanged.subscribe{
                    if (it.hasChanged && it.propertyName == "trackDistanceUnitFormatterTypeName") {
                        this.trackDistanceUnitFormatter = this.distanceUnitFormatterFactory.createTrackDistanceUnitFormatter()
                    }
                }
        )

        this.trackListActivity.tracklistactivity_trackrecordings_grid.adapter = itemsAdapter

        itemsAdapter.subscribeTo(itemsObservable)

        /*
        this.trackListActivity.startactivity_button_start_new_recording.clicks().subscribe{
            val intent = Intent(this.trackListActivity, TrackRecorderActivity::class.java)
            intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.StartNew.toString())

            this.trackListActivity.startActivity(intent)

            this.trackListActivity.finish()
        }
        */

        // TODO:
        val navigationView = this.trackListActivity.findViewById<NavigationView>(R.id.tracklistactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackListActivityPresenter.trackListActivity.startActivity(Intent(this@TrackListActivityPresenter.trackListActivity, SettingsActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_trackrecordings) {
                            val intent = Intent(this@TrackListActivityPresenter.trackListActivity, TrackListActivity::class.java)

                            this@TrackListActivityPresenter.trackListActivity.startActivity(intent)
                        }

                        this@TrackListActivityPresenter.trackListActivity.tracklistactivity_navigationdrawer.closeDrawer(this@TrackListActivityPresenter.trackListActivity.tracklistactivity_navigation)

                        return true
                    }
                })
    }

    public fun destroy() {
        this.subscriptions.clear()
    }
}