package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.content.*
import android.os.Build
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.location.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.IOException

internal final class TrackRecorderActivityViewModel(private val context: Context) {
    private var trackRecorderService: ITrackRecorderService? = null

    private var trackRecordingSession: ITrackRecordingSession? = null

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val currentTrackRecordingStore: IDataStore<TrackRecording> = CurrentTrackRecordingStore(context)

    private val trackRecorderServiceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val self = this@TrackRecorderActivityViewModel

            self.trackRecorderService = service as ITrackRecorderService
            self.trackRecordingSession = self.trackRecorderService!!.currentSession

            if (self.trackRecordingSession == null) {
                try {
                    val restoredTrackRecording = self.currentTrackRecordingStore.getData()
                    if (restoredTrackRecording != null) {
                        self.trackRecordingSession = self.trackRecorderService?.createSession(restoredTrackRecording)
                    }
                } catch(exception: IOException) {
                    self.currentTrackRecordingStore.delete()

                    Log.e("TrackRecorderActivityVM", "Unable to restore saved state of current recording! App still works but unfortunately you have lost your last recording :(", exception)
                }
            }

            if (self.trackRecordingSession != null) {
                self.subscribeToSession()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            val self = this@TrackRecorderActivityViewModel

            self.unsubscribeFromSession()

            self.trackRecordingSession = null
            self.trackRecorderService = null
        }
    }

    init {
        this.startService()
    }

    private fun subscribeToSession() {
        this.subscriptions.addAll(
            this.trackRecordingSession!!.recordingTimeChanged.subscribe {
                currentRecordingTime ->
                    this.recordingTimeChangedSubject.onNext(currentRecordingTime)
            },

            this.trackRecordingSession!!.trackDistanceChanged.subscribe{
                currentTrackDistance ->
                    this.trackDistanceChangedSubject.onNext(currentTrackDistance)
            },

            this.trackRecordingSession!!.stateChanged.subscribe {
                currentState ->
                    this.trackSessionStateChangedSubject.onNext(currentState)

                    val isRunning = currentState == TrackRecorderServiceState.Running
                    val isPaused = currentState == TrackRecorderServiceState.Paused

                    this.canStartResumeRecordingSubject.onNext(!isRunning)
                    this.canPauseRecordingSubject.onNext(isRunning)
                    this.canDiscardRecordingSubject.onNext(isPaused)
                    this.canFinishRecordingSubject.onNext(isPaused)
                    this.canShowTrackAttachmentsSubject.onNext(!isRunning && isPaused)
            }
        )

        this.locationChangedAvailableSubject.onNext(this.trackRecordingSession!!.locationsChanged)
    }

    private fun unsubscribeFromSession() {
        this.subscriptions.clear()

        this.locationChangedAvailableSubject.onNext(Observable.never())
    }

    public fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.context.startForegroundService(Intent(this.context, TrackRecorderService::class.java))
        } else {
            this.context.startService(Intent(this.context, TrackRecorderService::class.java))
        }

        this.context.bindService(Intent(this.context, TrackRecorderService::class.java), this.trackRecorderServiceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    public fun terminateService() {
        this.context.unbindService(this.trackRecorderServiceConnection)

        this.context.stopService(Intent(this.context, TrackRecorderService::class.java))
    }

    public fun saveCurrentRecording() {
        if(this.trackRecordingSession == null) {
            throw IllegalStateException()
        }

        this.trackRecordingSession!!.saveTracking()
    }

    private val recordingTimeChangedSubject: BehaviorSubject<Period> = BehaviorSubject.createDefault(Period.ZERO)
    public val recordingTimeChanged: Observable<Period> = this.recordingTimeChangedSubject

    private val trackDistanceChangedSubject: BehaviorSubject<Float> = BehaviorSubject.createDefault(0.0f)
    public val trackDistanceChanged: Observable<Float> = this.trackDistanceChangedSubject

    private val trackSessionStateChangedSubject: BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault(TrackRecorderServiceState.Initializing)
    public val trackSessionStateChanged: Observable<TrackRecorderServiceState> = this.trackSessionStateChangedSubject

    private val locationChangedAvailableSubject: BehaviorSubject<Observable<Location>> = BehaviorSubject.createDefault<Observable<Location>>(Observable.never())
    public val locationsChangedAvailable: Observable<Observable<Location>> = this.locationChangedAvailableSubject

    private val canStartResumeRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(true)
    public val canStartResumeRecordingChanged: Observable<Boolean> = this.canStartResumeRecordingSubject

    public fun startResumeRecording() {
        if (this.trackRecordingSession == null) {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
            val nameTemplate = this.context.getString(R.string.trackrecorderactivity_viewmodel_default_new_trackrecording_name_template)

            val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

            this.trackRecordingSession = this.trackRecorderService!!.createSession(trackRecordingName)

            this.subscribeToSession()
        }

        this.trackRecordingSession!!.resumeTracking()
    }

    private var canPauseRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canPauseRecordingChanged: Observable<Boolean> = this.canPauseRecordingSubject

    public fun pauseRecording() {
        this.trackRecordingSession!!.pauseTracking()
    }

    private var canDiscardRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canDiscardRecordingChanged: Observable<Boolean> = this.canDiscardRecordingSubject

    public fun discardRecording() {
        // TODO: ViewModel or Activity?
        val alertBuilder = AlertDialog.Builder(this.context)
        alertBuilder.setTitle("TITEL")
        alertBuilder.setCancelable(true)
        alertBuilder.setMessage("NACHRICHT?")
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertBuilder.setNegativeButton("NEIN!", {
            dialog: DialogInterface?,
            button: Int ->
                dialog!!.dismiss()
        })
        alertBuilder.setPositiveButton("JA!!", {
            dialog: DialogInterface?,
            button: Int ->
                this.trackRecorderService!!.discardTracking()

                this.unsubscribeFromSession()
                this.trackRecordingSession = null
        })

        alertBuilder.show()
    }

    private var canFinishRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canFinishRecordingChanged: Observable<Boolean> = this.canFinishRecordingSubject

    public fun finishRecording() {
        // TODO: ViewModel or Activity?
        val alertBuilder = AlertDialog.Builder(this.context)
        alertBuilder.setTitle("TITEL")
        alertBuilder.setCancelable(true)
        alertBuilder.setMessage("NACHRICHT?")
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertBuilder.setNegativeButton("NEIN!", {
            dialog: DialogInterface?,
            button: Int ->
            dialog!!.dismiss()
        })
        alertBuilder.setPositiveButton("JA!!", {
            dialog: DialogInterface?,
            button: Int ->
            this.trackRecorderService!!.finishTracking()

            this.unsubscribeFromSession()
            this.trackRecordingSession = null
        })

        alertBuilder.show()
    }

    private var canShowTrackAttachmentsSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canShowTrackAttachmentsChanged: Observable<Boolean> = this.canShowTrackAttachmentsSubject

    public fun showTrackAttachments() {
    }
}