package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_data_tab.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class DataTabFragmentPresenter(private val dataTabFragment: DataTabFragment,
                                              private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>,
                                              private val appSettings: IAppSettings,
                                              private val trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory) {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter

    private var currentLocationsCount: AtomicInteger = AtomicInteger()

    init {
        this.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
            } else {
                this.uninitializeSession()
            }
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.dataTabFragment.trackrecorderactivity_tab_data_startedat.text().accept(trackRecorderSession.trackingStartedAt.formatDefault())
        this.dataTabFragment.trackrecorderactivity_tab_data_trackname.text().accept(trackRecorderSession.name)
        this.dataTabFragment.trackrecorderactivity_tab_data_comments.text().accept(trackRecorderSession.comment)

        this.sessionSubscriptions.addAll(
            Observable.switchOnNext(Observable.fromArray(trackRecorderSession.stateChanged
                    .filter {
                        it == TrackRecorderServiceState.Initializing
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
                    .subscribe(this.dataTabFragment.trackrecorderactivity_fragment_data_tab_locationscount.text()),

            this.appSettings.appSettingsChanged.subscribe{
                if(it.propertyName == "trackDistanceUnitFormatterTypeName" && it.hasChanged) {
                    this.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()
                }
            },
            this.dataTabFragment.trackrecorderactivity_tab_data_trackname.textChanges().subscribe{
                trackRecorderSession.name = it.toString()
            },

            this.dataTabFragment.trackrecorderactivity_tab_data_comments.textChanges().subscribe{
                trackRecorderSession.comment = it.toString()
            },

            trackRecorderSession.trackDistanceChanged.map {
                this.trackDistanceUnitFormatter.format(it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this.dataTabFragment.trackrecorderactivity_tab_data_trackdistance.text()),

            trackRecorderSession.recordingTimeChanged.map {
                it.formatRecordingTime()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this.dataTabFragment.trackrecorderactivity_tab_data_recordingtime.text())
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.dataTabFragment.trackrecorderactivity_tab_data_startedat.text().accept(this.dataTabFragment.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none))
        this.dataTabFragment.trackrecorderactivity_tab_data_trackname.text().accept("")
        this.dataTabFragment.trackrecorderactivity_tab_data_comments.text().accept("")

        this.trackRecorderSession = null
    }

    public fun destroy() {
        this.trackRecorderServiceControllerSubscription.dispose()

        this.uninitializeSession()
    }
}