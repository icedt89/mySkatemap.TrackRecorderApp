package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicInteger

internal final class DataTabFragment : Fragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var presenter: TrackRecorderActivityPresenter

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
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_distance))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_recordingtime))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_locationscount))
                .store(view.findViewById<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_trackingstartedat))
    }

    public override fun onStart() {
        super.onStart()

        this.subscriptions.addAll(
                this.presenter.trackDistanceChanged.map {
                    it.formatTrackDistance(this.context!!)
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_distance).text()),

                this.presenter.recordingTimeChanged.map {
                    it.formatRecordingTime()
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_recordingtime).text()),

                this.presenter.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if(it == TrackRecorderServiceState.Initializing) {
                        this.currentLocationsCount.set(0)

                        this.locationsCountSubject.onNext(this.currentLocationsCount.get())
                    }
                },

                this.presenter.locationsChangedAvailable.observeOn(AndroidSchedulers.mainThread()).subscribe{
                    this.currentLocationsChangedSubscription?.dispose()

                    // TODO: Performance: Use buffered!
                    this.currentLocationsChangedSubscription = it.map {
                        this.currentLocationsCount.incrementAndGet()
                    }
                            .observeOn(AndroidSchedulers.mainThread()).subscribe{
                        this.locationsCountSubject.onNext(it)
                    }
                },

                this.locationsCountSubject.map {
                    it.toString()
                }.subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_locationscount).text()),

                this.presenter.trackingStartedAtChanged.map {
                    if(it == DateTime(0)) {
                        this.context!!.getText(R.string.trackrecorderactivity_fragment_data_tab_trackingstartedat_none)
                    } else {
                        it.formatDefault()
                    }
                }.observeOn(AndroidSchedulers.mainThread()).subscribe(this.viewHolder.retrieve<AppCompatTextView>(R.id.trackrecorderactivity_fragment_data_tab_trackingstartedat).text())
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

    public override fun setPresenter(presenter: TrackRecorderActivityPresenter) {
        this.presenter = presenter
    }
}