package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.services.getByIdOrDefaultAsync
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfileSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.single.BasePermissionListener
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.util.*


internal fun Context.getApplicationInjector(): ApplicationComponent {
    val result = this.applicationContext as TrackRecorderApplication

    return result.injector
}

internal fun AppCompatActivity.checkAllAppPermissions() : Single<Boolean> {
    return Single.create { emitter: SingleEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BaseMultiplePermissionsListener() {
                    public override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        emitter.onSuccess(report!!.areAllPermissionsGranted())
                    }
                })
                .check()
    }
}

internal fun AppCompatActivity.checkWriteExternalStoragePermission(): Single<Boolean> {
    return Single.create { emitter: SingleEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BasePermissionListener() {
                    public override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        emitter.onSuccess(true)
                    }

                    public override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        emitter.onSuccess(false)
                    }
                })
                .check()
    }
}

internal fun AppCompatActivity.checkAccessFineLocationPermission(): Single<Boolean> {
    return Single.create { emitter: SingleEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : BasePermissionListener() {
                    public override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        emitter.onSuccess(true) }

                    public override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        emitter.onSuccess(false)
                    }
                })
                .check()
    }
}

internal fun ICrudRepository<Dashboard>.getByIdOrDefaultAsync(id: UUID?, default: Dashboard = Dashboard(UUID.randomUUID())) : Single<Dashboard> {
    return this.getByIdOrDefaultAsync(id, default)
}

internal fun IUserProfileSettings.isValidForBurnedEnergyCalculation() : Boolean {
    return this.enableCalculationOfBurnedEnergy && this.age != null && this.weight != null && this.height != null && this.sex != null
}