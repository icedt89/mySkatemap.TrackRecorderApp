package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.support.annotation.ColorInt
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.KilometersTrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*

internal final class AppSettings: IAppSettings {
    private val appSettingsChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()

    public override val appSettingsChanged: Observable<PropertyChangedData> = this.appSettingsChangedSubject

    public override var currentTrackRecordingId: UUID? = null
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("currentTrackRecordingId", oldValue, value))
        }

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

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences) : IAppSettings {
        return SharedPreferencesAppSettingsBinding(this, sharedPreferences)
    }

    private final class SharedPreferencesAppSettingsBinding(private val appSettings: IAppSettings, sharedPreferences: SharedPreferences) : IAppSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        private val appSettingsChangedSubscription: Disposable

        public override var trackDistanceUnitFormatterTypeName: String
            get() = this.appSettings.trackDistanceUnitFormatterTypeName
            set(value) {
                this.appSettings.trackDistanceUnitFormatterTypeName = value
            }

        public override var vibrateOnBackgroundStop: Boolean
            get() = this.appSettings.vibrateOnBackgroundStop
            set(value) {
                this.appSettings.vibrateOnBackgroundStop = value
            }

        public override var locationProviderTypeName: String
            get() = this.appSettings.locationProviderTypeName
            set(value) {
                this.appSettings.locationProviderTypeName = value
            }

        public override var notificationFlashColorOnBackgroundStop: Int
            get() = this.appSettings.notificationFlashColorOnBackgroundStop
            set(value) {
                this.appSettings.notificationFlashColorOnBackgroundStop = value
            }

        public override var appUiLocale: String
            get() = this.appSettings.appUiLocale
            set(value) {
                this.appSettings.appUiLocale = value
            }

        public override var allowLiveTracking: Boolean
            get() = this.appSettings.allowLiveTracking
            set(value) {
                this.appSettings.allowLiveTracking = value
            }

        public override var currentTrackRecordingId: UUID?
            get() = this.appSettings.currentTrackRecordingId
            set(value) {
                this.appSettings.currentTrackRecordingId = value
            }

        public override val appSettingsChanged: Observable<PropertyChangedData>
            get() = this.appSettings.appSettingsChanged

        init {
            val currentTrackRecordingId = sharedPreferences.getString("currentTrackRecordingId", null)
            if(currentTrackRecordingId != null) {
                this.appSettings.currentTrackRecordingId = UUID.fromString(currentTrackRecordingId)
            }

            this.appSettingsChangedSubscription = this.appSettings.appSettingsChanged.subscribe {
                if(it.hasChanged && it.propertyName == "currentTrackRecordingId") {
                    val sharedPreferenceEditor = sharedPreferences.edit()
                    if(it.newValue != null) {
                        sharedPreferenceEditor.putString(it.propertyName, it.newValue.toString())
                    } else {
                        sharedPreferenceEditor.remove(it.propertyName)
                    }

                    sharedPreferenceEditor.apply()
                }
            }

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                when(key) {
                    "preference_units_distance" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME)
                        this.appSettings.trackDistanceUnitFormatterTypeName = currentValue
                    }
                    "preference_tracking_location_provider" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPE_NAME)
                        this.appSettings.locationProviderTypeName = currentValue
                    }
                    "preference_notifications_vibrate_on_background_stop" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP)
                        this.appSettings.vibrateOnBackgroundStop = currentValue
                    }
                    "preference_notifications_notification_flash_color_on_background_stop" -> {

                    }
                    "preference_app_ui_locale" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_APP_UI_LOCALE)
                        this.appSettings.appUiLocale = currentValue
                    }
                    "preference_tracking_allow_live_tracking" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
                        this.appSettings.allowLiveTracking = currentValue
                    }
                }
            }

            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.appSettings.trackDistanceUnitFormatterTypeName = sharedPreferences.getString("preference_units_distance", AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME)
            this.appSettings.locationProviderTypeName = sharedPreferences.getString("preference_tracking_location_provider", AppSettings.DEFAULT_LOCATION_PROVIDER_TYPE_NAME)
            this.appSettings.vibrateOnBackgroundStop = sharedPreferences.getBoolean("preference_notifications_vibrate_on_background_stop", AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP)
            this.appSettings.appUiLocale = sharedPreferences.getString("preference_app_ui_locale", AppSettings.DEFAULT_APP_UI_LOCALE)
            this.appSettings.allowLiveTracking = sharedPreferences.getBoolean("preference_tracking_allow_live_tracking", AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
        }
    }

    companion object {
        public val DEFAULT_LOCATION_PROVIDER_TYPE_NAME: String = FusedLocationProvider::class.java.name

        public val DEFAULT_ALLOW_LIVE_TRACKING: Boolean = false

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public const val DEFAULT_VIBRATE_ON_BACKGROUND_STOP: Boolean = true

        @ColorInt
        public const val DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP: Int = Color.RED

        public val DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPE_NAME: String = KilometersTrackDistanceUnitFormatter::class.java.name
    }
}