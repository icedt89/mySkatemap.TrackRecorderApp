package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.textChanges
import com.janhafner.myskatemap.apps.trackrecorder.common.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.common.subscribeToList
import com.janhafner.myskatemap.apps.trackrecorder.common.withCount
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.format
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.format
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.IActivityDetectorBroadcastReceiverFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.ILocationAvailabilityChangedDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_debug_tab.*
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

internal final class DebugTabFragmentPresenter(private val view: DebugTabFragment,
                                               private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                               private val appSettings: IAppSettings,
                                               private val distanceConverterFactory: IDistanceConverterFactory,
                                               private val energyConverterFactory: IEnergyConverterFactory,
                                               private val speedConverterFactory: ISpeedConverterFactory,
                                               private val activityDetectorBroadcastReceiverFactory: IActivityDetectorBroadcastReceiverFactory,
                                               private val locationAvailabilityChangedDetector: ILocationAvailabilityChangedDetector) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var distanceConverter: IDistanceConverter

    private var energyConverter: IEnergyConverter

    private var speedConverter: ISpeedConverter

    private val logOutputItemsAdapter: LogOutputItemsAdapter = LogOutputItemsAdapter()

    private var filterRegex: Regex? = null

    init {
        this.view.trackrecorderactivity_tab_debug_log_output_items.adapter = this.logOutputItemsAdapter
        this.view.trackrecorderactivity_tab_debug_log_output_items.layoutManager = LinearLayoutManager(this.view.context)

        this.distanceConverter = this.distanceConverterFactory.createConverter()
        this.energyConverter = this.energyConverterFactory.createConverter()
        this.speedConverter = this.speedConverterFactory.createConverter()

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
                                this.distanceConverter = this.distanceConverterFactory.createConverter()
                            }

                            if (it.propertyName == IAppSettings::energyUnitFormatterTypeName.name) {
                                this.energyConverter = this.energyConverterFactory.createConverter()
                            }

                            if (it.propertyName == IAppSettings::speedUnitFormatterTypeName.name) {
                                this.speedConverter = this.speedConverterFactory.createConverter()
                            }
                        },
                this.logOutputItemsAdapter.arrayChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.view.trackrecorderactivity_tab_debug_log_output_items.smoothScrollToPosition(this.logOutputItemsAdapter.itemCount)
                        }
        )
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        val activityDetectorBroadcastReceiver = this.activityDetectorBroadcastReceiverFactory.createActivityDetector()

        val logOutputObservable = trackRecorderSession.locationsChanged
                .withCount()
                .map {
                    "Received location #${it.count}"
                }
                .mergeWith(trackRecorderSession.stateChanged.map {
                        var result = "Session changed state to ${it.state}"
                        if (it.pausedReason != null) {
                            result = "${result} because ${it.pausedReason}"
                        }

                        result
                })
                .mergeWith(this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .map {
                            "AppSetting property \"${it.propertyName}\" changed from \"${it.oldValue}\" to \"${it.newValue}\""
                })
                .mergeWith(activityDetectorBroadcastReceiver.activityDetected
                        .map {
                            "Activity Detector detected ${it.detectedActivityType} with ${it.confidence} confidence"
                        })
                .mergeWith(trackRecorderSession.burnedEnergyChanged
                        .sample(30, TimeUnit.SECONDS)
                        .map {
                            val formattedBurnedEnergy = this.energyConverter.format(it.kiloCalories)
                            "Current burned energy: ${formattedBurnedEnergy}"
                        })
                .mergeWith(trackRecorderSession.distanceChanged.map {
                    val formattedDistance = this.distanceConverter.format(it)
                    "Current distance: ${formattedDistance}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.speed.maximumValueChanged.map {
                    val formattedSpeed = this.speedConverter.format(it)
                    "Maximum speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.speed.minimumValueChanged.map {
                    val formattedSpeed = this.speedConverter.format(it)
                    "Minimum speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.speed.lastValueChanged.map {
                    val formattedSpeed = this.speedConverter.format(it)
                    "Last speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.speed.firstValueChanged.map {
                    val formattedSpeed = this.speedConverter.format(it)
                    "First speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.speed.averageValueChanged.map {
                    val formattedSpeed = this.speedConverter.format(it)
                    "Average speed: ${formattedSpeed}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.altitude.maximumValueChanged.map {
                    val formattedAltitude = this.distanceConverter.format(it)
                    "Maximum altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.altitude.minimumValueChanged.map {
                    val formattedAltitude = this.distanceConverter.format(it)
                    "Minimum altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.altitude.lastValueChanged.map {
                    val formattedAltitude = this.distanceConverter.format(it)
                    "Last altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.altitude.firstValueChanged.map {
                    val formattedAltitude = this.distanceConverter.format(it)
                    "First altitude: ${formattedAltitude}"
                })
                .mergeWith(trackRecorderSession.locationsAggregation.altitude.averageValueChanged.map {
                    val formattedAltitude = this.distanceConverter.format(it)
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
                        .observeOn(AndroidSchedulers.mainThread()))
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