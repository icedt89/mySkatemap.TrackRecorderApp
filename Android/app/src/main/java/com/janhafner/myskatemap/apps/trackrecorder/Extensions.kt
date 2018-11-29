package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.janhafner.myskatemap.apps.trackrecorder.common.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.common.pairWithPrevious
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.withCount
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfileSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.Seconds
import java.util.concurrent.TimeUnit

internal fun Context.isGooglePlayServicesAvailable() : Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val isGooglePlayServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return isGooglePlayServicesAvailable == ConnectionResult.SUCCESS
}

public fun Context.getFusedLocationProviderClient(): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(this)
}

public fun Context.getInputMethodManager(): InputMethodManager {
    return this.getSystemService(InputMethodManager::class.java)
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

internal fun Context.getApplicationInjector(): ApplicationComponent {
    val result = this.applicationContext as TrackRecorderApplication

    return result.injector
}

internal fun Context.getManifestVersionName(): String {
    return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
}

internal fun AppCompatActivity.checkAllAppPermissions() : Single<Boolean> {
    return Single.fromPublisher<Boolean>{
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BaseMultiplePermissionsListener() {
                    public override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        it.onNext(report!!.areAllPermissionsGranted())

                        it.onComplete()
                    }
                })
                .check()
    }
}

internal fun IUserProfileSettings.isValidForBurnedEnergyCalculation() : Boolean {
    return this.age != null && this.weight != null && this.height != null && this.sex != null
}