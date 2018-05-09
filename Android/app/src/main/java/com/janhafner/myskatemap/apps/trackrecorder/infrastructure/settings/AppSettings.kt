package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.support.annotation.ColorInt
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.KilometersTrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

internal final class AppSettings: IAppSettings {
    public override var allowLiveTracking: Boolean = DEFAULT_ALLOW_LIVE_TRACKING
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("allowLiveTracking", oldValue, value))
        }

    public override var appUiLocale: String = DEFAULT_APP_UI_LOCALE
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("appUiLocale", oldValue, value))
        }

    public override var trackDistanceUnitFormatterTypeName: String = DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("trackDistanceUnitFormatterTypeName", oldValue, value))
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

        public val DEFAULT_ALLOW_LIVE_TRACKING: Boolean = false

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public const val DEFAULT_VIBRATE_ON_BACKGROUND_STOP: Boolean = true

        @ColorInt
        public const val DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP: Int = Color.RED

        public val DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME: String = KilometersTrackDistanceUnitFormatter::class.java.name

        public lateinit var sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        public fun bindToSharedPreferences(targetSharedPreferences: SharedPreferences): IAppSettings {
            val result = AppSettings()

            AppSettings.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                when(key) {
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
                    "preference_app_ui_locale" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_APP_UI_LOCALE)
                        result.appUiLocale = currentValue
                    }
                    "preference_tracking_allow_live_tracking" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
                        result.allowLiveTracking = currentValue
                    }
                }
            }

            targetSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            result.trackDistanceUnitFormatterTypeName = targetSharedPreferences.getString("preference_units_distance", AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME)
            result.locationProviderTypeName = targetSharedPreferences.getString("preference_tracking_location_provider", AppSettings.DEFAULT_LOCATION_PROVIDER_TYPE_NAME)
            result.vibrateOnBackgroundStop = targetSharedPreferences.getBoolean("preference_notifications_vibrate_on_background_stop", AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP)
            result.appUiLocale = targetSharedPreferences.getString("preference_app_ui_locale", AppSettings.DEFAULT_APP_UI_LOCALE)
            result.allowLiveTracking = targetSharedPreferences.getBoolean("preference_tracking_allow_live_tracking", AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)

            return result
        }
    }
}