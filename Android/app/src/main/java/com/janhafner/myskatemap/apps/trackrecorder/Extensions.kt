package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import com.janhafner.myskatemap.apps.trackrecorder.io.ContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.single.BasePermissionListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.functions.Consumer
import org.joda.time.DateTime

internal fun Context.startLocationSourceSettingsActivity() {
    this.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

internal fun Context.getApplicationInjector(): ApplicationComponent {
    val result = this.applicationContext as TrackRecorderApplication

    return result.injector
}

internal fun Location.clone(sequenceNumber: Int): Location {
    val result = Location(sequenceNumber)

    result.latitude = this.latitude
    result.longitude = this.longitude
    result.provider = this.provider
    result.bearing = this.bearing
    result.speed = this.speed
    result.accuracy = this.accuracy
    result.bearingAccuracyDegrees = this.bearingAccuracyDegrees
    result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond
    result.verticalAccuracyMeters = this.verticalAccuracyMeters
    result.altitude = this.altitude

    return result
}

internal fun <TSource> Observable<TSource>.pairWithPrevious() : Observable<Pair<TSource?, TSource?>> {
    return this.scan(Pair<TSource?, TSource?>(null, null), {
        t1, t2 ->
            Pair(t1.second, t2!!)
    })
    .filter {
        // Filter seed [Pair(null, null)]
        it.second != null
    }
}

internal fun Observable<Location>.dropLocationsNotInDistance(maximumDistance: Double, includeEdge: Boolean = true): Observable<Location> {
    return this.pairWithPrevious()
            .filter{
                if (it.first == null) {
                    // Dont filter first real value [Pair(null, location)]
                    true
                } else {
                    // Apply distance filter to suppress emitting values to close
                    !it.second!!.isInDistance(it.first!!, maximumDistance, includeEdge)
                }
            }
            .map {
                it.second
            }
}

internal fun Location.isInDistance(location: Location, maximumDistance: Double, includeEdge: Boolean = true): Boolean {
    val distance = this.distanceTo(location)

    if (includeEdge) {
        return distance <= maximumDistance
    }

    return distance < maximumDistance
}

private fun Location.toLiteAndroidLocation(): android.location.Location {
    val result = android.location.Location(this.provider)

    if (this.bearing != null) {
        result.bearing = this.bearing!!
    }

    result.latitude = this.latitude
    result.longitude = this.longitude

    return result
}

internal fun android.location.Location.toLocation(sequenceNumber: Int): Location {
    val result = Location(sequenceNumber)

    result.altitude = this.altitude
    result.latitude = this.latitude
    result.longitude = this.longitude

    result.speed = this.speed
    result.provider = this.provider
    result.capturedAt = DateTime(this.time)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result.bearingAccuracyDegrees = this.bearingAccuracyDegrees
        result.accuracy = this.accuracy
        result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond
        result.verticalAccuracyMeters = this.verticalAccuracyMeters
    }

    return result
}

internal fun Location.toSimpleLocation(): SimpleLocation {
    return SimpleLocation(this.latitude, this.longitude)
}

internal fun ITrackRecorderMap.consumeLocations(): Consumer<List<Location>> {
    return Consumer({
        val locations = it.map { it.toSimpleLocation() }
        this.addLocations(locations)
    })
}

internal fun Context.isLocationServicesEnabled(): Boolean {
    return Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
}

internal fun Location.distanceTo(location: Location): Float {
    val androidLocation = this.toLiteAndroidLocation()
    val otherAndroidLocation = location.toLiteAndroidLocation()

    return androidLocation.distanceTo(otherAndroidLocation)
}

internal fun Activity.checkAllAppPermissions(): Observable<Boolean> {
    return Observable.create { emitter: ObservableEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BaseMultiplePermissionsListener() {
                    public override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        emitter.onNext(report!!.areAllPermissionsGranted())

                        emitter.onComplete()
                    }
                })
                .check()
    }
}

internal fun Activity.checkAccessFineLocationPermission(): Observable<Boolean> {
    return Observable.create { emitter: ObservableEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : BasePermissionListener() {
                    public override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        emitter.onNext(true)

                        emitter.onComplete()
                    }

                    public override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        emitter.onNext(false)

                        emitter.onComplete()
                    }
                })
                .check()
    }
}

internal fun Activity.checkWriteExternalStoragePermission(): Observable<Boolean> {
    return Observable.create { emitter: ObservableEmitter<Boolean> ->
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : BasePermissionListener() {
                    public override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        emitter.onNext(true)

                        emitter.onComplete()
                    }

                    public override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        emitter.onNext(false)

                        emitter.onComplete()
                    }
                })
                .check()
    }
}

internal fun ContentResolver.getContentInfo(uri: Uri): ContentInfo {
    val cursor = this.query(uri, arrayListOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE).toTypedArray(), null, null, null)

    val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    val sizeColumnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

    cursor.moveToFirst()

    val displayName = cursor.getString(displayNameColumnIndex)
    val size =  cursor.getLong(sizeColumnIndex)
    val mimeType = this.getType(uri)

    cursor.close()

    return ContentInfo(displayName, size, uri, mimeType)
}