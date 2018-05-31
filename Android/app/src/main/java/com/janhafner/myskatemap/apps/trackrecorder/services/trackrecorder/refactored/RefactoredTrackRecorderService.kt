package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.NullBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.statistics.TrackRecordingStatistic
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

internal final class RefactoredTrackRecorderService : Service(), ITrackRecorderService {
    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var appConfig: IAppConfig

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory

    @Inject
    public lateinit var locationProviderFactory: ILocationProviderFactory

    @Inject
    public lateinit var metActivityDefinitionFactory: IMetActivityDefinitionFactory

    @Inject
    public lateinit var liveLocationTrackingServiceFactory: ILiveLocationTrackingServiceFactory

    private var sessionSubscription: Disposable? = null

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        this.trackRecorderServiceNotification = RefactoredTrackRecorderServiceNotification(this, this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter())

        super.onCreate()
    }

    public override var currentSession: ITrackRecordingSession? = null
        private set(value) {
            if(value != null && value != field) {
                this.sessionSubscription = value.sessionClosed.subscribe{
                    this.currentSession = null
                }
            }

            if(field != null && value == null) {
                this.sessionSubscription?.dispose()
                this.sessionSubscription = null
            }

            this.hasCurrentSessionChangedSubject.onNext(value != null)
        }

    private val hasCurrentSessionChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public override val hasCurrentSessionChanged: Observable<Boolean>
        get() = this.hasCurrentSessionChangedSubject

    private lateinit var trackRecorderServiceNotification: RefactoredTrackRecorderServiceNotification

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException("Tracking already in progress!")
        }

        val locationProvider = this.locationProviderFactory.createLocationProvider(trackRecording.locationProviderTypeName)
        val observableTimer = ObservableTimer()
        val trackRecordingStatistic = TrackRecordingStatistic()
        val trackDistanceCalculator = TrackDistanceCalculator()
        val burnedEnergyCalculator: IBurnedEnergyCalculator
        val liveLocationTrackingSession: ILiveLocationTrackingSession

        if(trackRecording.fitnessActivity != null) {
            // TODO: Activity?
            val metActivityDefinition = this.metActivityDefinitionFactory.getMetActivityDefinitionByCode(trackRecording.fitnessActivity!!.metActivityCode)
            if(metActivityDefinition != null) {
                burnedEnergyCalculator  = BurnedEnergyCalculator(trackRecording.fitnessActivity!!.weightInKilograms,
                        trackRecording.fitnessActivity!!.heightInCentimeters,
                        trackRecording.fitnessActivity!!.age,
                        trackRecording.fitnessActivity!!.sex,
                        metActivityDefinition.metValue)
            } else {
                burnedEnergyCalculator = NullBurnedEnergyCalculator()
            }
        } else {
            burnedEnergyCalculator = NullBurnedEnergyCalculator()
        }

        val liveLocationTrackingService = this.liveLocationTrackingServiceFactory.createService()
        liveLocationTrackingSession = liveLocationTrackingService.createSession()

        this.currentSession = RefactoredTrackRecordingSession(
                this.appSettings,
                this.appConfig,
                this.trackService,
                this.trackRecorderServiceNotification,
                trackDistanceCalculator,
                this.trackDistanceUnitFormatterFactory,
                trackRecording,
                trackRecordingStatistic,
                burnedEnergyCalculator,
                locationProvider,
                observableTimer,
                liveLocationTrackingSession,
                this)

        this.appSettings.currentTrackRecordingId = trackRecording.id

        return this.currentSession!!
    }

    public override fun onBind(intent: Intent?): IBinder {
        this.serviceBound()

        return TrackRecorderServiceBinder(this)
    }

    public override fun onRebind(intent: Intent?) {
        this.serviceBound()
    }

    private fun serviceBound() {
        this.trackRecorderServiceNotification.isBound = true

        this.tryUpdateNotification()
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.serviceUnbind()

        return true
    }

    private fun serviceUnbind() {
        this.trackRecorderServiceNotification.isBound = false

        this.tryUpdateNotification()
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if(notification != null) {
            this.startForeground(RefactoredTrackRecorderServiceNotification.ID, notification)
        }
    }

    private var isDestroyed: Boolean = false
    public override fun onDestroy() {
        if(this.isDestroyed) {
            return
        }

        if(this.currentSession != null) {
            this.currentSession!!.destroy()
        }

        this.hasCurrentSessionChangedSubject.onComplete()

        this.isDestroyed = true
    }
}

