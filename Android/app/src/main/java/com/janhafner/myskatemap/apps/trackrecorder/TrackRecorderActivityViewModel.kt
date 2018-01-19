package com.janhafner.myskatemap.apps.trackrecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

internal final class TrackRecorderActivityViewModel(private val context: Context) {
    private var trackRecorderService: ITrackRecorderService? = null

    private var subscriptions: CompositeDisposable = CompositeDisposable()

    private val currentTrackRecordingStore : IDataStore<TrackRecording> = CurrentTrackRecordingStore(context)

    private val trackRecorderServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val self = this@TrackRecorderActivityViewModel

            self.trackRecorderService = service as ITrackRecorderService

            self.subscriptions.addAll(self.trackRecorderServiceStateChanged.subscribe({ state: TrackRecorderServiceState ->
                if (state == TrackRecorderServiceState.Ready || state == TrackRecorderServiceState.Paused) {
                    self.subscriptions.add(self.trackRecorderService!!.locations.subscribe({ location: Location ->
                        self.locationsObservable.onNext(location)
                    }))
                }

                val isActive : Boolean = state == TrackRecorderServiceState.Running

                self.canPauseRecordingObservable.onNext(isActive)
                self.canStartRecordingObservable.onNext(!isActive)
                self.canDiscardCurrentTrackRecordingObservable.onNext(!isActive)
                self.canFinishCurrentTrackRecordingObservable.onNext(!isActive)
            }))

            var restoredRecording  = self.currentTrackRecordingStore.getData()
            if(restoredRecording != null) {
                self.trackRecorderService!!.useTrackRecording(restoredRecording)
            }

            self.isReadySubject.onNext(true)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            val self = this@TrackRecorderActivityViewModel

            self.isReadySubject.onNext(false)

            self.trackRecorderService = null

            self.subscriptions.clear()
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

    private val canStartRecordingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public val canStartRecording: Observable<Boolean>
        get() = this.canStartRecordingObservable

    public fun startRecording() {
        // Service was NOT initialized during binding with a restored tracking!
        if(this.trackRecorderService!!.state == TrackRecorderServiceState.Initializing) {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
            val nameTemplate = this.context.getString(R.string.trackrecorderactivityviewmodel_default_new_trackrecording_name_template)

            var trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

            this.trackRecorderService!!.useTrackRecording(trackRecordingName)
        }

        this.trackRecorderService!!.resumeTracking()
    }

    private val canPauseRecordingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public val canPauseRecording: Observable<Boolean>
        get() = this.canPauseRecordingObservable

    public fun pauseRecording() {
        this.trackRecorderService?.pauseTracking()
    }

    private val canDiscardCurrentTrackRecordingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public val canDiscardCurrentTrackRecording: Observable<Boolean>
        get() = this.canDiscardCurrentTrackRecordingObservable

    public fun discardCurrentTrackRecording() {
        this.trackRecorderService!!.pauseTracking()
        this.trackRecorderService!!.discardTracking()
    }

    private val canFinishCurrentTrackRecordingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public val canFinishCurrentTrackRecording: Observable<Boolean>
        get() = this.canFinishCurrentTrackRecordingObservable

    public fun finishCurrentTrackRecording() {
        this.trackRecorderService!!.pauseTracking()

        // TODO: Save as not uploaded history item
    }

    private val isReadySubject : Subject<Boolean> = BehaviorSubject.createDefault(false)
    public val isReady : Observable<Boolean>
        get() = this.isReadySubject

    public val trackRecorderServiceStateChanged: Observable<TrackRecorderServiceState>
        get() = this.trackRecorderService!!.stateChanged

    private val locationsObservable : Subject<Location> = ReplaySubject.create<Location>()
    public val locations: Observable<Location>
        get() = this.locationsObservable

    public fun dispose() {
        this.context.stopService(Intent(this.context, TrackRecorderService::class.java))

        this.context.unbindService(this.trackRecorderServiceConnection)
    }

    public fun saveCurrentTrackRecording() {
        this.trackRecorderService?.saveTracking()
    }
}