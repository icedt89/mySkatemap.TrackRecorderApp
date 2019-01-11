package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.graphics.Color
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

internal final class PlaygroundActivityPresenter(private val view: PlaygroundActivity, private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    init {
        this.view.setContentView(R.layout.activity_playground)

        this.subscriptions.addAll(
                this.trackRecorderServiceController.isClientBoundChanged
                        .subscribeOn(Schedulers.computation())
                        .flatMap {
                            if(it) {
                                this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                            } else {
                                Observable.just(false)
                            }
                        }
                        .subscribe {
                            if (it) {
                                this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                            } else {
                                this.uninitializeSession()
                            }
                        }
        )

        this.view.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.add(trackRecorderSession.stateChanged
                        .subscribeOn(Schedulers.computation())
                .map {
                    if(it.state == TrackRecordingSessionState.Running) {
                        0
                    } else {
                        if(it.pausedReason == TrackingPausedReason.StillStandDetected) {
                            1
                        } else {
                            2
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    val view = this.view.findViewById<AppCompatTextView>(R.id.playground_isstill)

                    when (it) {
                        0 -> {
                            view.setBackgroundColor(Color.GREEN)
                            view.text = "ALG_#2: MOVING!"

                            Log.i("STILLDETECTION", "ALG_#2: MOVING")
                        }
                        1 -> {
                            view.setBackgroundColor(Color.RED)
                            view.text = "ALG_#2: STILL!"

                            Log.i("STILLDETECTION", "ALG_#2: STILL")
                        }
                        2 -> {
                            view.setBackgroundColor(Color.YELLOW)
                            view.text = "ALG_#2: PAUSED!"

                            Log.i("STILLDETECTION", "ALG_#2: PAUSED")
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
        this.subscriptions.dispose()

        this.view.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}