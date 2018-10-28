package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.content.Context
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityType
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfileSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import io.reactivex.Observable
import io.reactivex.Single

internal fun  <T : Fragment> Fragment.findChildFragmentById(@IdRes id: Int) : T {
    return this.childFragmentManager.fragments.first {
        it.id == id
    } as T
}

/**
 * Returns an Observable that tries to determine if the current activity type is assumed as "still".
 *
 * @return an Observable that emits true if "still" is assumed, otherweise, false.
 */
internal fun Observable<ActivityType>.isStill() : Observable<Boolean> {
    return this.map {
        it == ActivityType.Still || (it != ActivityType.OnBicycle && it != ActivityType.OnFoot && it != ActivityType.Running && it != ActivityType.Walking)
    }
}

internal fun Context.getActivityName(code: String): String {
    val codesArray = this.resources.getStringArray(R.array.appsettings_preference_default_met_activity_code_values)
    val index = codesArray.indexOf(code)

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