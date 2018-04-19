package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.content.Context
import android.support.v4.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppConfig

internal final class TrackRecorderMapFragmentFactory(private val context: Context, private val appConfig: IAppConfig) : ITrackRecorderMapFragmentFactory {
    override fun getFragment(): Fragment {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val isGooglePlayServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this.context)
        if(isGooglePlayServicesAvailable == ConnectionResult.SUCCESS && !this.appConfig.forceUsingOpenStreetMap) {
            return GoogleTrackRecorderMapFragment()
        }

        return OpenStreetMapTrackRecorderMapFragment()
    }
}