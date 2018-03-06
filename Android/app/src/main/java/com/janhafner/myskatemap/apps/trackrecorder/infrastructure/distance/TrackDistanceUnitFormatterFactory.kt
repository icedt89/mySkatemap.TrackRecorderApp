package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.graphics.Color
import android.support.annotation.ColorInt
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.FusedLocationProvider
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal interface ITrackDistanceUnitFormatterFactory {
    fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter
}

internal interface IAppSettingsChanged {
    val appSettingsChanged: Observable<PropertyChangedData>
}

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var trackColor: Int

    var vibrateOnBackgroundStop: Boolean

    var locationProviderTypeName: String

    var notificationFlashColorOnBackgroundStop: Int
}

internal final class AppSettings: IAppSettings {
    public override var trackDistanceUnitFormatterTypeName: String = AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME
        set(value) {
            val oldValue = field

            field = value

            if(oldValue != value) {
                this.appSettingsChangedSubject.onNext(PropertyChangedData("trackDistanceUnitFormatterTypeName", oldValue, value))
            }
        }

    public override var trackColor: Int = Color.RED
        set(value) {
            val oldValue = field

            field = value

            if(oldValue != value) {
                this.appSettingsChangedSubject.onNext(PropertyChangedData("trackColor", oldValue, value))
            }
        }

    public override var vibrateOnBackgroundStop: Boolean = AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP
        set(value) {
            val oldValue = field

            field = value

            if(oldValue != value) {
                this.appSettingsChangedSubject.onNext(PropertyChangedData("vibrateOnBackgroundStop", oldValue, value))
            }
        }

    public override var notificationFlashColorOnBackgroundStop: Int = AppSettings.DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP
        set(value) {
            val oldValue = field

            field = value

            if(oldValue != value) {
                this.appSettingsChangedSubject.onNext(PropertyChangedData("notificationFlashColorOnBackgroundStop", oldValue, value))
            }
        }

    public override var locationProviderTypeName: String = AppSettings.DEFAULT_LOCATION_PROVIDER_TYPE_NAME
        set(value) {
            val oldValue = field

            field = value

            if(oldValue != value) {
                this.appSettingsChangedSubject.onNext(PropertyChangedData("locationProviderTypeName", oldValue, value))
            }
        }

    private val appSettingsChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val appSettingsChanged: Observable<PropertyChangedData> = this.appSettingsChangedSubject

    companion object {
        public val DEFAULT_LOCATION_PROVIDER_TYPE_NAME: String = FusedLocationProvider::javaClass.name

        public val DEFAULT_VIBRATE_ON_BACKGROUND_STOP: Boolean = true

        @ColorInt
        public val DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP: Int = Color.RED

        @ColorInt
        public val DEFAULT_TRACK_COLOR: Int = Color.parseColor("#FFFF3A3C")

        public val DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME: String = KilometersTrackDistanceUnitFormatter::javaClass.name
    }
}

internal final class PropertyChangedData(public val propertyName: String, public val oldValue: Any, public val newValue: Any) {
}

internal final class TrackDistanceUnitFormatterFactory(private val appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
    private val logTag: String = this.javaClass.simpleName

    private val milesTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = MilesTrackDistanceUnitFormatter()

    private val kilometersTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = KilometersTrackDistanceUnitFormatter()

    public override fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter {
        if(this.appSettings.trackDistanceUnitFormatterTypeName == MilesTrackDistanceUnitFormatter::javaClass.name) {
            return this.milesTrackDistanceUnitFormatter
        }

        if(this.appSettings.trackDistanceUnitFormatterTypeName != KilometersTrackDistanceUnitFormatter::javaClass.name) {
            Log.wtf(this.logTag, "Not a valid TrackDistanceUnitFormatter implementation: \"${this.appSettings.trackDistanceUnitFormatterTypeName}\"")
        }

        return this.kilometersTrackDistanceUnitFormatter
    }
}