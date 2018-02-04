package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.formatTrackDistance
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicInteger

internal final class DataTabFragment : Fragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var viewModel: TrackRecorderActivityViewModel

    private var currentLocationsCount: AtomicInteger = AtomicInteger()

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var currentLocationsChangedSubscription: Disposable? = null

    private val locationsCountSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_data_tab, container, false)
    }

    public override fun onDetach() {
        super.onDetach()

        this.currentLocationsCount.set(0)
        this.subscriptions.clear()
        this.currentLocationsChangedSubscription?.dispose()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trackDistanceTextView = this.view!!.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_distance)
        val trackRecordingTimeTextView = this.view!!.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_recordingtime)
        val trackLocationsCountTextView = this.view!!.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_locationscount)
        val trackingStartedAtTextView = this.view!!.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_trackingstartedat)

        this.subscriptions.addAll(
                this.viewModel.trackDistanceChanged.map {
                    currentTrackDistance ->
                        currentTrackDistance.formatTrackDistance(this.context)
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(trackDistanceTextView.text()),

                this.viewModel.recordingTimeChanged.map {
                    currentRecordingTime ->
                        currentRecordingTime.formatRecordingTime()
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(trackRecordingTimeTextView.text()),

                this.viewModel.trackSessionStateChanged.subscribe {
                    currentState ->
                    if(currentState == TrackRecorderServiceState.Initializing) {
                        this.currentLocationsCount.set(0)

                        this.locationsCountSubject.onNext(this.currentLocationsCount.get())
                    }
                },

                this.viewModel.locationsChangedAvailable.subscribe{
                    locationsChangedObservable ->
                        this.currentLocationsChangedSubscription?.dispose()

                        this.currentLocationsChangedSubscription = locationsChangedObservable.map {
                            this.currentLocationsCount.incrementAndGet()
                        }.observeOn(AndroidSchedulers.mainThread()).subscribe{
                            this.locationsCountSubject.onNext(it)
                        }
                },

                this.locationsCountSubject.map {
                    it.toString()
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(trackLocationsCountTextView.text()),

                this.viewModel.trackingStartedAtChanged.map {
                    if(it == DateTime(0)) {
                        this.context.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none)
                    } else {
                        it.formatDefault()
                    }
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(trackingStartedAtTextView.text())
        )
    }

    public override fun setViewModel(viewModel: TrackRecorderActivityViewModel) {
        this.viewModel = viewModel
    }
}