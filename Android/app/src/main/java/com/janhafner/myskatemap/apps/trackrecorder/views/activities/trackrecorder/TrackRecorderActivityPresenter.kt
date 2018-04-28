package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.start.StartActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_track_recorder.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

internal final class TrackRecorderActivityPresenter(private val trackRecorderActivity: TrackRecorderActivity,
                                                    private val trackService: ITrackService,
                                                    private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>) {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var finishTrackRecordingMenuItem: MenuItem? = null

    private var discardTrackRecordingMenuItem: MenuItem? = null

    init {
        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                val uninitializedSession: ITrackRecordingSession

                val mode = ActivityStartMode.valueOf(this.trackRecorderActivity.intent.getStringExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY))
                val newTrackRecordingCreated = mode == ActivityStartMode.StartNew || (mode == ActivityStartMode.TryResume && !this.trackService.hasCurrentTrackRecording())
                if (newTrackRecordingCreated) {
                    val newTrackRecording = this.createNewTrackRecording()

                    uninitializedSession = binder.useTrackRecording(newTrackRecording)
                } else {
                    val restoredTrackRecording = this.trackService.getCurrentTrackRecording()

                    uninitializedSession = binder.useTrackRecording(restoredTrackRecording)
                }

                this.trackRecorderSession = this.getInitializedSession(uninitializedSession)

                if(newTrackRecordingCreated){
                    if (this.trackRecorderActivity.isLocationServicesEnabled()) {
                        this.trackRecorderSession!!.resumeTracking()
                    } else {
                        ShowLocationServicesSnackbar.make(this.trackRecorderActivity, this.trackRecorderActivity.currentFocus).show()
                    }
                }
            } else {
                this.uninitializeSession()
            }
        }
    }

    private fun createNewTrackRecording(): TrackRecording {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
        val nameTemplate = this.trackRecorderActivity.getString(R.string.trackrecorderactivity_presenter_default_new_trackrecording_name_template)

        val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

        return TrackRecording.start(trackRecordingName)
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
            this.trackRecorderActivity.trackrecorderactivity_togglerecording_floatingactionbutton.clicks()
                  .subscribe {
                      trackRecorderSession.stateChanged.first(TrackRecorderServiceState.Initializing).subscribe {
                          state ->
                          if(state == TrackRecorderServiceState.Paused) {
                              if(this.trackRecorderActivity.isLocationServicesEnabled()) {
                                  trackRecorderSession.resumeTracking()
                              }else{
                                  ShowLocationServicesSnackbar.make(this.trackRecorderActivity, this.trackRecorderActivity.currentFocus).show()
                              }
                          } else {
                              trackRecorderSession.pauseTracking()
                          }
                      }
                  },


                trackRecorderSession.stateChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.trackRecorderActivity.trackrecorderactivity_togglerecording_floatingactionbutton.isEnabled = it == TrackRecorderServiceState.Running || it == TrackRecorderServiceState.Paused

                            var iconId = R.drawable.ic_action_track_recorder_recording_startresume
                            if (it == TrackRecorderServiceState.Running) {
                                iconId = R.drawable.ic_action_track_recorder_recording_pause
                            }

                            this.trackRecorderActivity.trackrecorderactivity_togglerecording_floatingactionbutton.setImageResource(iconId)
                        },

                trackRecorderSession.stateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecorderServiceState.Running -> {
                            Toast.makeText(this.trackRecorderActivity, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        }
                        TrackRecorderServiceState.Paused,
                        TrackRecorderServiceState.LocationServicesUnavailable ->
                            Toast.makeText(this.trackRecorderActivity, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                        else -> {
                            // Nothing happens here. Else branch exist only to prevent warning on compile Oo
                        }
                    }

                    if(this.discardTrackRecordingMenuItem != null) {
                        this.discardTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecorderServiceState.Running
                    }

                    if(this.finishTrackRecordingMenuItem != null) {
                        this.finishTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecorderServiceState.Running
                    }
                }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null
    }

    public fun menuReady(menu: Menu) {
        if(this.finishTrackRecordingMenuItem == null) {
            this.finishTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking)

            this.sessionSubscriptions.add(this.finishTrackRecordingMenuItem!!.clicks().subscribe{
                val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.trackRecorderActivity)
                finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, {
                    _,
                    _ ->
                    // TODO: Make something useful with it (history, upload etc...)
                    val trackRecording = this.trackRecorderSession!!.finishTracking()

                    this.uninitializeSession()

                    this.trackRecorderActivity.startActivity(Intent(this.trackRecorderActivity, StartActivity::class.java))

                    this.trackRecorderActivity.finish()
                })

                finishRecordingAlertDialogBuilder.show()
            })
        }

        if(this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)

            this.sessionSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks().subscribe{
                        val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.trackRecorderActivity)
                        discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, {
                            _, _ ->
                            this.trackRecorderSession!!.discardTracking()

                            this.uninitializeSession()

                            this.trackRecorderActivity.startActivity(Intent(this.trackRecorderActivity, StartActivity::class.java))

                            this.trackRecorderActivity.finish()
                        })

                        discardRecordingAlertDialogBuilder.show()
                    })
        }
    }

    public fun destroy() {
        this.trackRecorderServiceControllerSubscription.dispose()

        this.uninitializeSession()
    }

    fun save() {
        this.trackRecorderSession?.stateChanged?.last(TrackRecorderServiceState.Initializing)!!.subscribe{
            it ->
            if(it != TrackRecorderServiceState.Initializing) {
                this.trackRecorderSession!!.saveTracking()
            }
        }
    }

    companion object {
        public const val ACTIVITY_START_MODE_KEY: String = "mode"
    }
}