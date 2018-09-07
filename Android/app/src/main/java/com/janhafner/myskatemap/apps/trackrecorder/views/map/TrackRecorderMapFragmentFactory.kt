package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class TrackRecorderMapFragmentFactory(private val context: Context, private val appSettings: IAppSettings) : ITrackRecorderMapFragmentFactory {
    public override fun createFragment(): TrackRecorderMapFragment {
        if(!BuildConfig.MAP_FORCE_OPENSTREETMAP_MAPCONTROL && this.appSettings.mapControlTypeName == GoogleTrackRecorderMapFragment::class.java.simpleName) {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val isGooglePlayServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this.context)
            if(isGooglePlayServicesAvailable == ConnectionResult.SUCCESS) {
                return GoogleTrackRecorderMapFragment()
            }
        }

        return OpenStreetMapTrackRecorderMapFragment()
    }
}