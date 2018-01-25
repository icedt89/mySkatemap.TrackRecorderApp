package com.janhafner.myskatemap.apps.trackrecorder.activities

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
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

internal final class TrackRecorderActivityViewModel(private val context : Context) {
    private var trackRecorderService: ITrackRecorderService? = null

    private var trackRecordingSession: ITrackRecordingSession? = null

    private var subscriptions: CompositeDisposable? = CompositeDisposable()

    private val currentTrackRecordingStore: IDataStore<TrackRecording> = CurrentTrackRecordingStore(context)

    private val trackRecorderServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val self = this@TrackRecorderActivityViewModel

            self.trackRecorderService = service as ITrackRecorderService
            self.trackRecordingSession = self.trackRecorderService!!.currentSession

            if(self.trackRecordingSession == null) {
                try {
                    var restoredTrackRecording = self.currentTrackRecordingStore.getData()
                    if(restoredTrackRecording != null) {
                        self.trackRecordingSession = self.trackRecorderService?.createSession(restoredTrackRecording)
                    }
                } catch(exception : Exception) {
                    Log.e("TrackRecorderActivityVM", "Unable to restore saved state of current recording! App still works but unfortunately you have lost your last recording :(", exception)
                }
            }

            if(self.trackRecordingSession != null) {
                self.subscribeToSession()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            val self = this@TrackRecorderActivityViewModel

            self.subscriptions?.dispose()
            self.subscriptions = null

            self.trackRecordingSession = null
            self.trackRecorderService = null
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.context.startForegroundService(Intent(this.context, TrackRecorderService::class.java))
        } else {
            this.context.startService(Intent(this.context, TrackRecorderService::class.java))
        }

        this.context.bindService(Intent(this.context, TrackRecorderService::class.java), this.trackRecorderServiceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private fun subscribeToSession() {
        this.unsubscribeFromSession()

        if(this.subscriptions == null) {
            this.subscriptions = CompositeDisposable()
        }

        this.subscriptions!!.addAll(
            this.trackRecordingSession!!.stateChanged.subscribe {
                currentState ->
                    this.trackSessionStateChangedSubject.onNext(currentState)

                    val isRunning = currentState == TrackRecorderServiceState.Running
                    val isPaused = currentState == TrackRecorderServiceState.Paused

                    this.canStartResumeRecordingSubject.onNext(!isRunning)
                    this.canPauseRecordingSubject.onNext(isRunning)
                    this.canDiscardRecordingSubject.onNext(!isRunning || isPaused)
                    this.canFinishRecordingSubject.onNext(!isRunning || isPaused)
                    this.canShowTrackAttachmentsSubject.onNext(!isRunning || isPaused)
            }
        )

        this.locationChangedAvailableSubject.onNext(this.trackRecordingSession!!.locationsChanged)
    }

    private fun unsubscribeFromSession() {
        if(this.subscriptions != null) {
            this.subscriptions?.dispose()
            this.subscriptions = null
        }

        this.locationChangedAvailableSubject.onNext(Observable.never())
    }

    private val trackSessionStateChangedSubject : BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault(TrackRecorderServiceState.Initializing)
    public val trackSessionStateChanged : Observable<TrackRecorderServiceState> = this.trackSessionStateChangedSubject.share()

    private val locationChangedAvailableSubject : BehaviorSubject<Observable<Location>> = BehaviorSubject.createDefault<Observable<Location>>(Observable.never())
    public val locationsChangedAvailable : Observable<Observable<Location>> = this.locationChangedAvailableSubject.share()

    private val canStartResumeRecordingSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(true)
    public val canStartResumeRecordingChanged : Observable<Boolean> = this.canStartResumeRecordingSubject.share()

    public fun startResumeRecording() {
        if(this.trackRecordingSession == null) {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
            val nameTemplate = this.context.getString(R.string.trackrecorderactivity_viewmodel_default_new_trackrecording_name_template)

            var trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

            this.trackRecordingSession = this.trackRecorderService!!.createSession(trackRecordingName)

            this.subscribeToSession()
        }

        this.trackRecordingSession!!.resumeTracking()
    }

    private var canPauseRecordingSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canPauseRecordingChanged : Observable<Boolean> = this.canPauseRecordingSubject.share()

    public fun pauseRecording() {
        this.trackRecordingSession!!.pauseTracking()
    }

    private var canDiscardRecordingSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canDiscardRecordingChanged : Observable<Boolean> = this.canDiscardRecordingSubject.share()

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

    private var canFinishRecordingSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canFinishRecordingChanged : Observable<Boolean> = this.canFinishRecordingSubject.share()

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

    private var canShowTrackAttachmentsSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canShowTrackAttachmentsChanged : Observable<Boolean> = this.canShowTrackAttachmentsSubject.share()

    public fun showTrackAttachments() {

    }
}