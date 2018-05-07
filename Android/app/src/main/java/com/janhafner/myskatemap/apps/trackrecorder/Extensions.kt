package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.ContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.JsonRestApiClient
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
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
import okhttp3.Response
import org.joda.time.DateTime

internal fun <TResponse> JsonRestApiClient.get(url: String, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "GET", null)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun JsonRestApiClient.delete(url: String): Response {
    return this.send(url, "DELETE", null)
}

internal fun <TRequest: Any, TResponse> JsonRestApiClient.post(url: String, body: TRequest, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "POST", body)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun <TRequest: Any> JsonRestApiClient.post(url: String, body: TRequest? = null): Response {
    return this.send(url, "POST", body)
}

internal fun <TRequest: Any, TResponse> JsonRestApiClient.put(url: String, body: TRequest, responseBodyClassType: Class<TResponse>): TResponse? {
    val response = this.send(url, "PUT", body)

    return this.fromJsonResponseBody(response.body(), responseBodyClassType)
}

internal fun <TRequest: Any> JsonRestApiClient.put(url: String, body: TRequest? = null): Response {
    return this.send(url, "PUT", body)
}

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
    var altitude = 0.0
    if(this.altitude != null){
        altitude = this.altitude!!
    }

    return SimpleLocation(this.latitude, this.longitude, altitude)
}

internal fun ITrackRecorderMap.consumeLocations(): Consumer<List<Location>> {
    return Consumer({
        val locations = it.map { it.toSimpleLocation() }
        this.addLocations(locations)
    })
}

internal fun ITrackRecorderMap.consumeReset(): Consumer<TrackRecorderServiceState> {
    return Consumer({
        currentState ->
            if (currentState == TrackRecorderServiceState.Idle) {
                this.clearTrack()
            }
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