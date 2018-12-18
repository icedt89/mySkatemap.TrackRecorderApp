package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.graphics.Color
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

internal final class PlaygroundActivityPresenter(private val view: PlaygroundActivity, private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    init {
        this.view.setContentView(R.layout.activity_playground)

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
                                                }
                                )
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
                        }
        )

        this.view.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        Log.i("STILLDETECTION", "Initialized with ${BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS}ms timeout and ${BuildConfig.FUSED_LOCATION_PROVIDER_SMALLEST_DISPLACEMENT_IN_METERS}m distance")



        this.sessionSubscriptions.add(trackRecorderSession.isStillChanged
                .filter {
                    !it
                }
                .map {
                    0
                }
                .mergeWith(trackRecorderSession.stateChanged
                        .filter {
                            it.state == TrackRecordingSessionState.Paused
                        }
                        .map {
                            if (it.pausedReason == TrackingPausedReason.StillStandDetected) {
                                1
                            } else {
                                2
                            }
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val view = this.view.findViewById<AppCompatTextView>(R.id.playground_isstill)

                    when (it) {
                        0 -> {
                            view.setBackgroundColor(Color.GREEN)
                            view.text = "ALG_#1: MOVING!"

                            Log.i("STILLDETECTION", "ALG_#1: MOVING")
                        }
                        1 -> {
                            view.setBackgroundColor(Color.RED)
                            view.text = "ALG_#1: STILL!"

                            Log.i("STILLDETECTION", "ALG_#1: STILL")
                        }
                        2 -> {
                            view.setBackgroundColor(Color.YELLOW)
                            view.text = "ALG_#1: PAUSED!"

                            Log.i("STILLDETECTION", "ALG_#1: PAUSED")
                        }
                    }
                })

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null
    }

    public fun destroy() {
        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()

        this.view.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}