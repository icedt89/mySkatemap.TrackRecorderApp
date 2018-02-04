package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.consumeReset
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragment: Fragment(), ITrackRecorderActivityDependantFragment, OnTrackRecorderMapReadyCallback {
    private lateinit var viewModel: TrackRecorderActivityViewModel

    private var currentLocationsChangedSubscription: Disposable? = null

    private lateinit var trackRecorderMap: ITrackRecorderMap

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecorderactivity_fragment_map_tab_map) as TrackRecorderMapFragment

        mapFragment.getMapAsync(this)
    }

    public override fun onDetach() {
        super.onDetach()

        this.subscriptions.clear()
        this.currentLocationsChangedSubscription?.dispose()
    }

    public override fun setViewModel(viewModel: TrackRecorderActivityViewModel) {
        this.viewModel = viewModel
    }

    public override fun onMapReady(trackRecorderMap: ITrackRecorderMap) {
        this.trackRecorderMap = trackRecorderMap

        this.trackRecorderMap!!.zoomToLocation(LatLng(50.8357, 12.92922), 12f)

        this.subscribeToMap()
    }

    private fun subscribeToMap() {
        this.subscriptions.addAll(
                this.viewModel.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe(this.trackRecorderMap.consumeReset()),

                this.viewModel.locationsChangedAvailable.subscribe{
                    locationsChangedObservable ->
                    this.currentLocationsChangedSubscription?.dispose()

                    this.currentLocationsChangedSubscription = locationsChangedObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .buffer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                            .subscribe(this.trackRecorderMap.consumeLocations())
                }
        )
    }
}

