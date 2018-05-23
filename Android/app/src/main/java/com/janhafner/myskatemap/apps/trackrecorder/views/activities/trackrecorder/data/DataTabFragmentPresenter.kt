package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_data_tab.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class DataTabFragmentPresenter(private val view: DataTabFragment,
                                              private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>,
                                              private val appSettings: IAppSettings,
                                              private val trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory) {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var sessionAvailabilityChangedSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter

    private var currentLocationsCount: AtomicInteger = AtomicInteger()

    init {
        this.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                this.sessionAvailabilityChangedSubscription = this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged.subscribe{
                    if(it) {
                        this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                    } else {
                        this.uninitializeSession()
                    }
                }
            } else {
                this.uninitializeSession()

                this.sessionAvailabilityChangedSubscription?.dispose()
            }
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.view.trackrecorderactivity_tab_data_startedat.text().accept(trackRecorderSession.trackingStartedAt.formatDefault())
        this.view.trackrecorderactivity_tab_data_trackname.text().accept(trackRecorderSession.name)
        this.view.trackrecorderactivity_tab_data_comments.text().accept(trackRecorderSession.comment)

        this.sessionSubscriptions.addAll(
            Observable.switchOnNext(Observable.fromArray(trackRecorderSession.stateChanged
                    .filter {
                        it == TrackRecorderServiceState.Idle
                    }
                    .map {
                        0
                    }, trackRecorderSession.locationsChanged
                    .buffer(1, TimeUnit.SECONDS)
                    .map {
                        this.currentLocationsCount.addAndGet(it.count())
                    }))
                    .map {
                        it.toString()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this.view.trackrecorderactivity_fragment_data_tab_locationscount.text()),

            this.appSettings.appSettingsChanged.subscribe{
                if(it.propertyName == "trackDistanceUnitFormatterTypeName" && it.hasChanged) {
                    this.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()
                }
            },

            this.view.trackrecorderactivity_tab_data_trackname.textChanges().subscribe{
                trackRecorderSession.name = it.toString()
            },

            this.view.trackrecorderactivity_tab_data_comments.textChanges().subscribe{
                trackRecorderSession.comment = it.toString()
            },

            trackRecorderSession.trackDistanceChanged.map {
                this.trackDistanceUnitFormatter.format(it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this.view.trackrecorderactivity_tab_data_trackdistance.text()),

            trackRecorderSession.recordingTimeChanged.map {
                it.formatRecordingTime()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this.view.trackrecorderactivity_tab_data_recordingtime.text())
        )

        return trackRecorderSession
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.view.trackrecorderactivity_tab_data_startedat.text().accept(this.view.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none))
        this.view.trackrecorderactivity_tab_data_trackname.text().accept("")
        this.view.trackrecorderactivity_tab_data_comments.text().accept("")

        this.trackRecorderSession = null
    }

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()

        this.trackRecorderServiceControllerSubscription.dispose()
        this.sessionAvailabilityChangedSubscription?.dispose()

        this.uninitializeSession()
    }
}