package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.support.annotation.ColorInt
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.KilometersTrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal final class AppSettings: IAppSettings {
    public override var trackDistanceUnitFormatterTypeName: String = DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("trackDistanceUnitFormatterTypeName", oldValue, value))
        }

    public override var mapStyleResourceName: String = DEFAULT_MAP_STYLE_RESOURCE_NAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("mapStyleResourceName", oldValue, value))
        }

    public override var trackColor: Int = Color.RED
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("trackColor", oldValue, value))
        }

    public override var vibrateOnBackgroundStop: Boolean = DEFAULT_VIBRATE_ON_BACKGROUND_STOP
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("vibrateOnBackgroundStop", oldValue, value))
        }

    public override var notificationFlashColorOnBackgroundStop: Int = DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("notificationFlashColorOnBackgroundStop", oldValue, value))
        }

    public override var locationProviderTypeName: String = DEFAULT_LOCATION_PROVIDER_TYPE_NAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("locationProviderTypeName", oldValue, value))
        }

    private val appSettingsChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val appSettingsChanged: Observable<PropertyChangedData> = this.appSettingsChangedSubject

    companion object {
        public val DEFAULT_LOCATION_PROVIDER_TYPE_NAME: String = FusedLocationProvider::class.java.name

        public const val DEFAULT_MAP_STYLE_RESOURCE_NAME: String = "mapstyle_fanticmotor"

        public const val DEFAULT_VIBRATE_ON_BACKGROUND_STOP: Boolean = true

        @ColorInt
        public const val DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP: Int = Color.RED

        @ColorInt
        public val DEFAULT_TRACK_COLOR: Int = Color.parseColor("#FFFF3A3C")

        public val DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME: String = KilometersTrackDistanceUnitFormatter::class.java.name

        public fun bindToSharedPreferences(targetSharedPreferences: SharedPreferences): IAppSettings {
            val result = AppSettings()

            targetSharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                when(key) {
                    "preference_map_style" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_MAP_STYLE_RESOURCE_NAME)
                        result.mapStyleResourceName = currentValue
                    }
                    "preference_map_track_color" -> {

                    }
                    "preference_units_distance" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME)
                        result.trackDistanceUnitFormatterTypeName = currentValue
                    }
                    "preference_tracking_location_provider" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPE_NAME)
                        result.locationProviderTypeName = currentValue
                    }
                    "preference_notifications_vibrate_on_background_stop" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP)
                        result.vibrateOnBackgroundStop = currentValue
                    }
                    "preference_notifications_notification_flash_color_on_background_stop" -> {

                    }
                }
            }

            return result
        }
    }
}