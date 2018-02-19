package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.IOException

internal final class TrackRecorderActivityPresenter(private val context: Context) : ITrackRecorderActivityPresenter {
    private var trackRecorderService: ITrackRecorderService? = null

    private var trackRecordingSession: ITrackRecordingSession? = null

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val currentTrackRecordingStore: IDataStore<TrackRecording> = CurrentTrackRecordingStore(context)

    private val trackRecorderServiceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val self = this@TrackRecorderActivityPresenter

            self.trackRecorderService = service as ITrackRecorderService
            self.trackRecordingSession = self.trackRecorderService!!.currentSession

            if (self.trackRecordingSession == null) {
                try {
                    val restoredTrackRecording = self.currentTrackRecordingStore.getData()
                    if (restoredTrackRecording != null) {
                        self.trackRecordingSession = self.trackRecorderService?.resumeSession(restoredTrackRecording)
                    }
                } catch(exception: IOException) {
                    self.currentTrackRecordingStore.delete()

                    Log.e("TrackRecorderAPresenter", "Unable to restore saved state of current recording! App still works but unfortunately you have lost your last recording :(", exception)
                }
            }

            if (self.trackRecordingSession != null) {
                self.subscribeToSession()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            val self = this@TrackRecorderActivityPresenter

            self.unsubscribeFromSession()

            self.trackRecordingSession = null
            self.trackRecorderService = null
        }

        public override fun onBindingDied(name: ComponentName?) {
        }
    }

    private fun subscribeToSession() {
        this.trackingStartedAtChangedSubject.onNext(this.trackRecordingSession!!.trackingStartedAt)

        this.subscriptions.addAll(
            this.trackRecordingSession!!.recordingTimeChanged.subscribe {
                this.recordingTimeChangedSubject.onNext(it)
            },

            this.trackRecordingSession!!.trackDistanceChanged
                    .subscribe{
                this.trackDistanceChangedSubject.onNext(it)
            },

            this.trackRecordingSession!!.stateChanged.subscribe {
                this.trackSessionStateChangedSubject.onNext(it)

                val isRunning = it == TrackRecorderServiceState.Running
                val isPaused = it == TrackRecorderServiceState.Paused

                this.canStartResumeRecordingSubject.onNext(!isRunning)
                this.canPauseRecordingSubject.onNext(isRunning)
                this.canDiscardRecordingSubject.onNext(isPaused)
                this.canFinishRecordingSubject.onNext(isPaused)
            }
        )

        this.locationChangedAvailableSubject.onNext(this.trackRecordingSession!!.locationsChanged)
    }

    private fun unsubscribeFromSession() {
        this.subscriptions.clear()

        this.locationChangedAvailableSubject.onNext(Observable.never())
        this.trackingStartedAtChangedSubject.onNext(DateTime(0))
    }

    override fun startAndBindService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.context.startForegroundService(Intent(this.context, TrackRecorderService::class.java))
        } else {
            this.context.startService(Intent(this.context, TrackRecorderService::class.java))
        }

        this.context.bindService(Intent(this.context, TrackRecorderService::class.java), this.trackRecorderServiceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    override fun unbindService() {
        this.context.unbindService(this.trackRecorderServiceConnection)
    }

    override fun saveCurrentRecording() {
        if(this.trackRecordingSession == null) {
            throw IllegalStateException()
        }

        this.trackRecordingSession!!.saveTracking()
    }

    private val attachmentsChangedSubject: PublishSubject<List<Attachment>> = PublishSubject.create<List<Attachment>>()
    public override val attachmentsChanged: Observable<List<Attachment>> = this.attachmentsChangedSubject

    public override fun addAttachment(attachment: Attachment) {
        this.trackRecordingSession!!.attachments.add(attachment)

        this.attachmentsChangedSubject.onNext(this.trackRecordingSession!!.attachments)
    }

    public override fun removeAttachment(attachment: Attachment) {
        this.trackRecordingSession!!.attachments.remove(attachment)

        this.attachmentsChangedSubject.onNext(this.trackRecordingSession!!.attachments)
    }

    private val trackingStartedAtChangedSubject: BehaviorSubject<DateTime> = BehaviorSubject.createDefault(DateTime(0))
    override val trackingStartedAtChanged: Observable<DateTime> = this.trackingStartedAtChangedSubject

    private val recordingTimeChangedSubject: BehaviorSubject<Period> = BehaviorSubject.createDefault(Period.ZERO)
    override val recordingTimeChanged: Observable<Period> = this.recordingTimeChangedSubject

    private val trackDistanceChangedSubject: BehaviorSubject<Float> = BehaviorSubject.createDefault(0.0f)
    override val trackDistanceChanged: Observable<Float> = this.trackDistanceChangedSubject

    private val trackSessionStateChangedSubject: BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault(TrackRecorderServiceState.Initializing)
    override val trackSessionStateChanged: Observable<TrackRecorderServiceState> = this.trackSessionStateChangedSubject

    private val locationChangedAvailableSubject: BehaviorSubject<Observable<Location>> = BehaviorSubject.createDefault<Observable<Location>>(Observable.never())
    override val locationsChangedAvailable: Observable<Observable<Location>> = this.locationChangedAvailableSubject

    private val canStartResumeRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(true)
    override val canStartResumeRecordingChanged: Observable<Boolean> = this.canStartResumeRecordingSubject

    override fun startResumeRecording() {
        if (this.trackRecordingSession == null) {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
            val nameTemplate = this.context.getString(R.string.trackrecorderactivity_presenter_default_new_trackrecording_name_template)

            val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

            this.trackRecordingSession = this.trackRecorderService!!.createNewSession(trackRecordingName)

            this.subscribeToSession()
        }

        this.trackRecordingSession!!.resumeTracking()
    }

    private var canPauseRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    override val canPauseRecordingChanged: Observable<Boolean> = this.canPauseRecordingSubject

    override fun pauseRecording() {
        this.trackRecordingSession!!.pauseTracking()
    }

    private var canDiscardRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    override val canDiscardRecordingChanged: Observable<Boolean> = this.canDiscardRecordingSubject

    override fun discardRecording() {
        // TODO:Presenter or Activity?
        val alertBuilder = AlertDialog.Builder(this.context)
        alertBuilder.setTitle(R.string.trackrecorderactivity_discard_confirmation_title)
        alertBuilder.setCancelable(true)
        alertBuilder.setMessage(R.string.trackrecorderactivity_discard_confirmation_message)
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertBuilder.setNegativeButton(R.string.trackrecorderactivity_discard_confirmation_button_no_label, null)
        alertBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, {
            _, _ ->
                this.trackRecordingSession!!.discardTracking()

                this.unsubscribeFromSession()
                this.trackRecordingSession = null
        })

        alertBuilder.show()
    }

    private var canFinishRecordingSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    override val canFinishRecordingChanged: Observable<Boolean> = this.canFinishRecordingSubject

    override fun finishRecording() {
        // TODO: Presenter or Activity?
        val alertBuilder = AlertDialog.Builder(this.context)
        alertBuilder.setTitle(R.string.trackrecorderactivity_finish_confirmation_title)
        alertBuilder.setCancelable(true)
        alertBuilder.setMessage(R.string.trackrecorderactivity_finish_confirmation_message)
        alertBuilder.setIcon(android.R.drawable.ic_dialog_info)
        alertBuilder.setNegativeButton(R.string.trackrecorderactivity_finish_confirmation_button_no_label, null)
        alertBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, {
            _,
            _ ->
                val trackRecording = this.trackRecordingSession!!.finishTracking()

                this.unsubscribeFromSession()
                this.trackRecordingSession = null
        })

        alertBuilder.show()
    }
}