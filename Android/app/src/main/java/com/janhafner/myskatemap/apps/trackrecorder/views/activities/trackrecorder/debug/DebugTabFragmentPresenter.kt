package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.textChanges
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.IStillDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_debug_tab.*
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

internal final class DebugTabFragmentPresenter(private val view: DebugTabFragment,
                                               private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                               private val appSettings: IAppSettings,
                                               private val distanceUnitFormatterFactory: IDistanceUnitFormatterFactory,
                                               private val energyUnitFormatterFactory: IEnergyUnitFormatterFactory,
                                               private val speedUnitFormatterFactory: ISpeedUnitFormatterFactory,
                                               private val stillDetector: IStillDetector,
                                               private val locationAvailabilityChangedDetector: ILocationAvailabilityChangedDetector) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var distanceUnitFormatter: IDistanceUnitFormatter

    private var energyUnitFormatter: IEnergyUnitFormatter

    private var speedUnitFormatter: ISpeedUnitFormatter

    private val logOutputItemsAdapter: LogOutputItemsAdapter = LogOutputItemsAdapter()

    private var filterRegex: Regex? = null

    init {
        this.view.trackrecorderactivity_tab_debug_log_output_items.adapter = this.logOutputItemsAdapter
        this.view.trackrecorderactivity_tab_debug_log_output_items.layoutManager = LinearLayoutManager(this.view.context)

        this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()
        this.energyUnitFormatter = this.energyUnitFormatterFactory.createFormatter()
        this.speedUnitFormatter = this.speedUnitFormatterFactory.createFormatter()

        this.subscriptions.addAll(
                this.trackRecorderServiceController.isClientBoundChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.addAll(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {
                                                    if (it) {
                                                        this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                                                    } else {
                                                        this.uninitializeSession()
                                                    }

                                                    this@DebugTabFragmentPresenter.view.trackrecorderactivity_tab_debug_log_output_items.visibility().accept(it)
                                                    this@DebugTabFragmentPresenter.view.trackrecorderactivity_tab_debug_log_output_empty.visibility().accept(!it)
                                                }
                                )
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
                        },
                this.view.trackrecorderactivity_tab_debug_log_output_filter.textChanges()
                        .subscribe {
                            val filterValues = it.toString().split(',', ignoreCase = true)
                                    .filter {
                                        !it.isEmpty()
                                    }

                            if (filterValues.any()) {
                                filterRegex = Regex("(${filterValues.joinToString("|")})", RegexOption.IGNORE_CASE)
                            } else {
                                filterRegex = null
                            }
                        },
                this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .subscribe {
                            if (it.propertyName == IAppSettings::distanceUnitFormatterTypeName.name) {
                                this.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()
                            }

                            if (it.propertyName == IAppSettings::energyUnitFormatterTypeName.name) {
                                this.energyUnitFormatter = this.energyUnitFormatterFactory.createFormatter()
                            }

                            if (it.propertyName == IAppSettings::speedUnitFormatterTypeName.name) {
                                this.speedUnitFormatter = this.speedUnitFormatterFactory.createFormatter()
                            }
                        },
                this.logOutputItemsAdapter.subscribeTo(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .timestamp()
                        .map {
                            val emittedAt = DateTime(it.time())
                            LogItem(emittedAt, "AppSetting property \"${it.value().propertyName}\" changed from \"${it.value().oldValue}\" to \"${it.value().newValue}\"")
                        }
                        .withCount()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterNext {
                            this.view.trackrecorderactivity_tab_debug_log_output_items.smoothScrollToPosition(this.logOutputItemsAdapter.itemCount)
                        })
        )
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        val logOutputObservable = trackRecorderSession.locationsChanged
                        .withCount()
                        .map {
                            "Received location #${it.count}"
                        }
                .mergeWith(this.stillDetector.stillDetectedChanged.map {
                    if (it) {
                        "StillDetector detected device stillstand"
                    } else {
                        "StillDetector detected device motion"
                    }
                })
                .mergeWith(this.stillDetector.isDetectingChanged.map {
                    if (it) {
                        "StillDetector started detection"
                    } else {
                        "StillDectector stopped detection"
                    }
                })
                .mergeWith(trackRecorderSession.stateChanged.map {
                    "Session changed state to ${it}"
                })
                .mergeWith(trackRecorderSession.burnedEnergyChanged.map {
                    val formattedBurnedEnergy = this.energyUnitFormatter.format(it.kiloCalories)
                    "Current burned energy: ${formattedBurnedEnergy}"
                })
                .mergeWith(trackRecorderSession.distanceChanged.map {
                    val formattedDistance = this.distanceUnitFormatter.format(it)
                    "Current distance: ${formattedDistance}"
                })
                .mergeWith(trackRecorderSession.statistic.speed.maximumValueChanged.map {
                    val formattedSpeed = this.speedUnitFormatter.format(it)
                    "Maximum speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.statistic.speed.minimumValueChanged.map {
                    val formattedSpeed = this.speedUnitFormatter.format(it)
                    "Minimum speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.statistic.speed.lastValueChanged.map {
                    val formattedSpeed = this.speedUnitFormatter.format(it)
                    "Last speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.statistic.speed.firstValueChanged.map {
                    val formattedSpeed = this.speedUnitFormatter.format(it)
                    "First speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.statistic.speed.averageValueChanged.map {
                    val formattedSpeed = this.speedUnitFormatter.format(it)
                    "Average speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.statistic.altitude.maximumValueChanged.map {
                    val formattedAltitude = this.distanceUnitFormatter.format(it)
                    "Maximum altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.statistic.altitude.minimumValueChanged.map {
                    val formattedAltitude = this.distanceUnitFormatter.format(it)
                    "Minimum altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.statistic.altitude.lastValueChanged.map {
                    val formattedAltitude = this.distanceUnitFormatter.format(it)
                    "Last altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.statistic.altitude.firstValueChanged.map {
                    val formattedAltitude = this.distanceUnitFormatter.format(it)
                    "First altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.statistic.altitude.averageValueChanged.map {
                    val formattedAltitude = this.distanceUnitFormatter.format(it)
                    "Average altitude: ${formattedAltitude}"
                })
                .mergeWith(this.locationAvailabilityChangedDetector.locationAvailabilityChanged.map {
                    if (it) {
                        "Location Services are available"
                    } else {
                        "Location Services not available"
                    }
                })
                .timestamp()
                .map {
                    val emittedAt = DateTime(it.time())
                    LogItem(emittedAt, it.value())
                }
                .withCount()

        this.sessionSubscriptions.addAll(
                this.logOutputItemsAdapter.subscribeToList(logOutputObservable
                        .filter {
                            if (this.filterRegex != null) {
                                it.value.message.contains(this.filterRegex!!)
                            } else {
                                true
                            }
                        }
                        .buffer(500, TimeUnit.MILLISECONDS)
                        .filterNotEmpty()
                        .observeOn(AndroidSchedulers.mainThread()).doAfterNext {
                            this.view.trackrecorderactivity_tab_debug_log_output_items.smoothScrollToPosition(this.logOutputItemsAdapter.itemCount)
                        })
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.logOutputItemsAdapter.clear()

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