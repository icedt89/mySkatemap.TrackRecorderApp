package com.janhafner.myskatemap.apps.activityrecorder

import android.Manifest
import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.janhafner.myskatemap.apps.activityrecorder.settings.IUserProfileSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import io.reactivex.Observable
import io.reactivex.Single

internal fun Context.isGooglePlayServicesAvailable() : Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val isGooglePlayServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return isGooglePlayServicesAvailable == ConnectionResult.SUCCESS
}

internal fun LocationRequest.withDefaultBuildConfig(): LocationRequest {
    this.fastestInterval = BuildConfig.FUSED_LOCATION_PROVIDER_FASTEST_INTERVAL_IN_MILLISECONDS.toLong()
    this.interval = BuildConfig.FUSED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS.toLong()
    this.maxWaitTime = BuildConfig.FUSED_LOCATION_PROVIDER_MAX_WAIT_TIME_IN_MILLISECONDS.toLong()
    this.smallestDisplacement = BuildConfig.FUSED_LOCATION_PROVIDER_SMALLEST_DISPLACEMENT_IN_METERS
    this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    return this
}

public fun Context.getFusedLocationProviderClient(): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(this)
}

internal fun  <T : Fragment> Fragment.findChildFragmentById(@IdRes id: Int) : T {
    return this.childFragmentManager.fragments.first {
        it.id == id
    } as T
}

internal fun Context.getActivityName(code: String): String? {
    val codesArray = this.resources.getStringArray(R.array.appsettings_preference_default_met_activity_code_values)
    val index = codesArray.indexOf(code)
    if(index == -1) {
        return null
    }

    val namesArray = this.resources.getStringArray(R.array.appsettings_preference_default_met_activity_code_names)

    return namesArray[index]
}

internal fun com.github.clans.fab.FloatingActionMenu.menuClicks(): Observable<View> {
    return Observable.create {
        val emitter = it

        this.setOnMenuButtonClickListener {
            if(!emitter.isDisposed){
                emitter.onNext(it)
            }
        }
    }
}

internal fun com.github.clans.fab.FloatingActionMenu.menuLongClicks(): Observable<View> {
    return Observable.create {
        val emitter = it

        this.setOnMenuButtonLongClickListener {
            if(!emitter.isDisposed){
                emitter.onNext(it)
            }

            true
        }
    }
}

internal fun Context.getActivityDrawableResourceId(code: String): Int? {
    val codesArray = this.resources.getStringArray(R.array.appsettings_preference_default_met_activity_code_values)
    val index = codesArray.indexOf(code)
    if(index == -1) {
        return null
    }

    val typedArray = this.resources.obtainTypedArray(R.array.met_activity_code_drawables)
    try {
        val resourceId = typedArray.getResourceId(index, -1)


        if(resourceId == -1) {
            return null
        }

        return resourceId
    } finally {
        typedArray.recycle()
    }
}

internal fun Context.getApplicationInjector(): ApplicationComponent {
    val result = this.applicationContext as TrackRecorderApplication

    return result.injector
}

internal fun Context.getManifestVersionName(): String {
    return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName
}

internal fun AppCompatActivity.checkAllAppPermissions() : Single<Boolean> {
    return Single.create {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BaseMultiplePermissionsListener() {
                    public override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        it.onSuccess(report!!.areAllPermissionsGranted())
                    }
                })
                .check()
    }
}

internal fun IUserProfileSettings.isValidForBurnedEnergyCalculation() : Boolean {
    return this.age != null && this.weight != null && this.height != null && this.sex != null
}