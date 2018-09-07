package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_data_tab.*
import java.util.concurrent.TimeUnit

internal final class DataTabFragmentPresenter(private val view: DataTabFragment,
                                              private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                              private val appSettings: IAppSettings,
                                              private val distanceUnitFormatterFactory: IDistanceUnitFormatterFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var distanceUnitFormatter: IDistanceUnitFormatter

    init {
        this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()

        this.subscriptions.add(
                this.trackRecorderServiceController.isClientBoundChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.add(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {
                                                    if (it) {
                                                        this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                                                    } else {
                                                        this.uninitializeSession()
                                                    }
                                                })
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
                        })
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.view.trackrecorderactivity_tab_data_startedat.text().accept(trackRecorderSession.trackingStartedAt.formatDefault())
        this.view.trackrecorderactivity_tab_data_trackname.text().accept(trackRecorderSession.name)
        this.view.trackrecorderactivity_tab_data_comments.text().accept(trackRecorderSession.comment)

        this.sessionSubscriptions.addAll(
                Observable.just(0)
                        .mergeWith(trackRecorderSession.locationsChanged
                                .buffer(1, TimeUnit.SECONDS)
                                .filterNotEmpty()
                                .liveCount())
                                .map {
                                    it.toString()
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this.view.trackrecorderactivity_fragment_data_tab_locationscount.text()),
                this.appSettings.propertyChanged
                        .subscribe {
                            if (it.hasChanged && it.propertyName == IAppSettings::distanceUnitFormatterTypeName.name) {
                                this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()
                            }
                        },
                this.view.trackrecorderactivity_tab_data_trackname.textChanges()
                        .subscribe {
                            trackRecorderSession.name = it.toString()
                        },
                this.view.trackrecorderactivity_tab_data_comments.textChanges()
                        .subscribe {
                            trackRecorderSession.comment = it.toString()
                        },
                trackRecorderSession.distanceChanged.map {
                            this.distanceUnitFormatter.format(it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view.trackrecorderactivity_tab_data_trackdistance.text()),

                trackRecorderSession.recordingTimeChanged
                        .map {
                            it.formatRecordingTime()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view.trackrecorderactivity_tab_data_recordingtime.text())
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.view.trackrecorderactivity_tab_data_startedat.text().accept(this.view.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none))
        this.view.trackrecorderactivity_tab_data_trackname.text().accept("")
        this.view.trackrecorderactivity_tab_data_comments.text().accept("")
        this.view.trackrecorderactivity_tab_data_recordingtime.text().accept("")
        this.view.trackrecorderactivity_tab_data_trackdistance.text().accept("")
        this.view.trackrecorderactivity_fragment_data_tab_locationscount.text().accept("")

        this.trackRecorderSession = null
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    public fun destroy() {
        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}