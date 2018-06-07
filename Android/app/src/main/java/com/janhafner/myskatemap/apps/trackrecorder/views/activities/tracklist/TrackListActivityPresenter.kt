package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import com.couchbase.lite.internal.support.Log
import com.jakewharton.rxbinding2.support.v7.widget.itemClicks
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.checkAllAppPermissions
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings.SettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_track_list.*
import kotlinx.android.synthetic.main.activity_track_list_item.view.*

internal final class TrackListActivityPresenter(private val view: TrackListActivity,
                                                private val trackService: ITrackService,
                                                private val appSettings: IAppSettings,
                                                private val distanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory,
                                                private val metActivityDefinitionFactory: IMetActivityDefinitionFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter = distanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

    init {
        this.view.setContentView(R.layout.activity_track_list)

        this.view.setSupportActionBar(this.view.tracklistactivity_toolbar)

        this.view.checkAllAppPermissions().subscribe { areAllGranted ->
            if(areAllGranted) {
                this.forwardToTrackRecorderIfNecessary()
            } else {
                this.view.finishAndRemoveTask()
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
            val intent = Intent(this.view, TrackRecorderActivity::class.java)
            intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.TryResume.toString())

            this.view.startActivity(intent)

            this.view.finish()
        } else {
            this.setupActivity()
        }
    }

    private fun setupActivity() {
        val itemsAdapter = TrackListItemsAdapter()

        this.view.tracklistactivity_trackrecordings_recyclerview.adapter = itemsAdapter
        this.view.tracklistactivity_trackrecordings_recyclerview.layoutManager = LinearLayoutManager(this.view)

        this.subscriptions.addAll(
                itemsAdapter.viewHolderBound.subscribe{
                    val locations = it.item.locations.values.toList()

                    // TODO
                    val trackDistanceCalculator = TrackDistanceCalculator()
                    trackDistanceCalculator.addAll(locations)
                    it.view.activity_track_list_item_tracking_distance.text = this.trackDistanceUnitFormatter.format(trackDistanceCalculator.distance)
                    trackDistanceCalculator.clear()

                    it.view.activity_track_list_item_tracking_name.text = it.item.name
                    it.view.activity_track_list_item_tracking_duration.text = it.item.recordingTime.formatRecordingTime()
                    it.view.activity_track_list_item_tracking_startedat.text = it.item.trackingStartedAt.formatDefault()

                    if(it.item.fitnessActivity != null) {
                        val metActivityDefinition = this.metActivityDefinitionFactory.getMetActivityDefinitionByCode(it.item.fitnessActivity!!.metActivityCode)

                        // TODO
                        val burnedEnergyCalculator = BurnedEnergyCalculator(it.item.fitnessActivity!!.weightInKilograms,
                                it.item.fitnessActivity!!.heightInCentimeters,
                                it.item.fitnessActivity!!.age,
                                it.item.fitnessActivity!!.sex,
                                metActivityDefinition!!.metValue)
                        burnedEnergyCalculator.calculate(it.item.recordingTime.seconds)

                        it.view.activity_track_list_item_tracking_fitness_calories.text = burnedEnergyCalculator.calculatedValue!!.toString()
                    } else {
                        it.view.activity_track_list_item_tracking_fitness_calories.text = ""
                    }

                    if(it.item.isFinished) {
                        it.view.activity_track_list_item_tracking_finishedat.text = it.item.trackingFinishedAt!!.formatDefault()
                    }

                    val trackRecording = it.item

                    val popupMenuAnchor = it.view.activity_track_list_item_tracking_menu
                    this.subscriptions.add(
                        popupMenuAnchor.clicks().subscribe {
                            val popupMenu = PopupMenu(this.view, popupMenuAnchor)
                            popupMenu.inflate(R.menu.track_list_activity_item_popupmenu)

                            this.subscriptions.add(
                                popupMenu.itemClicks().subscribe {
                                    if(it.itemId == R.id.track_list_activity_item_popupmenu_recording_show) {
                                        Log.i("TRACKLIST", "Needs to be implemented!")

                                    } else if(it.itemId == R.id.track_list_activity_item_popupmenu_recording_delete) {
                                        try {
                                            this.trackService.deleteTrackRecording(trackRecording.id.toString())

                                            Log.i("TRACKLIST", "Refresh bound array list after deletion went successful!")
                                        } catch(exception: Exception) {
                                            Log.i("TRACKLIST", "Exception handling!")
                                        }
                                    }
                                }
                            )

                            popupMenu.show()
                        }
                    )
                },

                appSettings.appSettingsChanged.subscribe{
                    if (it.hasChanged && it.propertyName == "trackDistanceUnitFormatterTypeName") {
                        this.trackDistanceUnitFormatter = this.distanceUnitFormatterFactory.createTrackDistanceUnitFormatter()
                    }
                }
        )

        val items = this.trackService.getAllTrackRecordings()

        itemsAdapter.addAll(items)

        this.view.tracklistactivity_main_floatingactionbutton.clicks().subscribe{
            val intent = Intent(this.view, TrackRecorderActivity::class.java)
            intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.StartNew.toString())

            this.view.startActivity(intent)

            this.view.finish()
        }

        // TODO:
        val navigationView = this.view.findViewById<NavigationView>(R.id.tracklistactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackListActivityPresenter.view.startActivity(Intent(this@TrackListActivityPresenter.view, SettingsActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_trackrecordings) {
                            val intent = Intent(this@TrackListActivityPresenter.view, TrackListActivity::class.java)

                            this@TrackListActivityPresenter.view.startActivity(intent)
                        }

                        this@TrackListActivityPresenter.view.tracklistactivity_navigationdrawer.closeDrawer(this@TrackListActivityPresenter.view.tracklistactivity_navigation)

                        return true
                    }
                })
    }

    public fun destroy() {
        this.subscriptions.clear()
    }
}