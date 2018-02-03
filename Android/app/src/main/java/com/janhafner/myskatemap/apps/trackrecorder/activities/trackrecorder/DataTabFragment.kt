package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.consumeReset
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class DataTabFragment : Fragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var viewModel: TrackRecorderActivityViewModel

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var currentLocationsChangedObservable: Disposable? = null

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_data_tab, container, false)
    }

    public override fun setViewModel(viewModel: TrackRecorderActivityViewModel) {
        this.viewModel = viewModel

        this.subscriptions.addAll(
            this.viewModel.trackDistanceChanged.subscribe {
            },

            this.viewModel.recordingTimeChanged.subscribe {

            },

            this.viewModel.locationsChangedAvailable.subscribe{
                locationsChangedObservable ->
                    this.currentLocationsChangedObservable?.dispose()

                    this.currentLocationsChangedObservable = locationsChangedObservable.
            }
        )
    }
}