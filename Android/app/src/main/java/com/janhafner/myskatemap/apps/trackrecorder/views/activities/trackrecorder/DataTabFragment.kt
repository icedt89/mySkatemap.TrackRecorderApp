package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class DataTabFragment : Fragment() {
    private lateinit var presenter: ITrackRecorderActivityPresenter

    private var currentLocationsCount: AtomicInteger = AtomicInteger()

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var currentLocationsChangedSubscription: Disposable? = null

    private val locationsCountSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    private val viewHolder: ViewHolder = ViewHolder()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_data_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewHolder
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_trackdistance))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_recordingtime))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_locationscount))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_startedat))
    }

    public override fun onStart() {
        super.onStart()

        this.subscriptions.addAll(
                this.presenter.trackDistanceChanged.map {
                    it.formatTrackDistance(this.context!!)
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_trackdistance).text()),

                this.presenter.recordingTimeChanged.map {
                    it.formatRecordingTime()
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_recordingtime).text()),

                this.presenter.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if(it == TrackRecorderServiceState.Initializing) {
                        this.currentLocationsCount.set(0)

                        this.locationsCountSubject.onNext(0)
                    }
                },

                this.presenter.locationsChangedAvailable.subscribe{
                    this.currentLocationsChangedSubscription?.dispose()

                    this.currentLocationsChangedSubscription = it.buffer(5, TimeUnit.SECONDS)
                            .map {
                                val count = it.count()
                                if(count > 0) {
                                    this.currentLocationsCount.addAndGet(count)
                                } else {
                                    -1
                                }
                            }.subscribe {
                                if(it != -1) {
                                    this.locationsCountSubject.onNext(it)
                                }
                            }
                },

                this.locationsCountSubject.map {
                    it.toString()
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_locationscount).text()),

                this.presenter.trackingStartedAtChanged.map {
                    if(it == DateTime(0)) {
                        this.context!!.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none)
                    } else {
                        it.formatDefault()
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_tab_data_startedat).text())
        )
    }

    public override fun onStop() {
        super.onStop()

        this.currentLocationsCount.set(0)
        this.subscriptions.clear()
        this.currentLocationsChangedSubscription?.dispose()
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.viewHolder.clear()
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(this.activity is TrackRecorderActivity) {
            this.presenter = (this.activity!! as TrackRecorderActivity).presenter
        }
    }
}