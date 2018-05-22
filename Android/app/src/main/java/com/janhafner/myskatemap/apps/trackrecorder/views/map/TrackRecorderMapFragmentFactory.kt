package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig

internal final class TrackRecorderMapFragmentFactory(private val context: Context, private val appConfig: IAppConfig) : ITrackRecorderMapFragmentFactory {
    public override fun getFragment(): TrackRecorderMapFragment {
        if (!this.appConfig.forceUsingOpenStreetMap){
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val isGooglePlayServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this.context)
            if(isGooglePlayServicesAvailable == ConnectionResult.SUCCESS) {
                return GoogleTrackRecorderMapFragment()
            }
        }

        return OpenStreetMapTrackRecorderMapFragment()
    }
}