package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

public final class AppSettings: IAppSettings {
    private val propertyChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val propertyChanged: Observable<PropertyChangedData> = this.propertyChangedSubject.subscribeOn(Schedulers.computation())

    public override var currentTrackRecordingId: UUID? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::currentTrackRecordingId.name, oldValue, value))
        }

    public override var vibrateOnLocationAvailabilityLoss: Boolean = DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::vibrateOnLocationAvailabilityLoss.name, oldValue, value))
        }

    public override var enableAutoPauseOnStill: Boolean = DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::enableAutoPauseOnStill.name, oldValue, value))
        }

    public override var mapControlTypeName: String = AppSettings.DEFAULT_MAP_CONTROL_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::mapControlTypeName.name, oldValue, value))
        }

    public override var currentDashboardId: UUID? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::currentDashboardId.name, oldValue, value))
        }

    public override var defaultMetActivityCode: String = AppSettings.DEFAULT_MET_ACTIVITY_CODE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::defaultMetActivityCode.name, oldValue, value))
        }

    public override var allowLiveTracking: Boolean = DEFAULT_ALLOW_LIVE_TRACKING
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::allowLiveTracking.name, oldValue, value))
        }

    public override var appUiLocale: String = DEFAULT_APP_UI_LOCALE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::appUiLocale.name, oldValue, value))
        }

    public override var distanceUnitFormatterTypeName: String = DEFAULT_DISTANCE_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::distanceUnitFormatterTypeName.name, oldValue, value))
        }

    public override var speedUnitFormatterTypeName: String = DEFAULT_SPEED_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::speedUnitFormatterTypeName.name, oldValue, value))
        }

    public override var energyUnitFormatterTypeName: String = DEFAULT_ENERGY_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::energyUnitFormatterTypeName.name, oldValue, value))
        }

    public override var locationProviderTypeName: String = DEFAULT_LOCATION_PROVIDER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::locationProviderTypeName.name, oldValue, value))
        }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences, context: Context): IAppSettings {
        return SharedPreferencesAppSettingsBinding(this, sharedPreferences, context)
    }

    private final class SharedPreferencesAppSettingsBinding(private val appSettings: IAppSettings, boundSharedPreferences: SharedPreferences, context: Context) : IAppSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        private val propertyChangedSubscription: Disposable

        public override var mapControlTypeName: String
            get() = this.appSettings.mapControlTypeName
            set(value) {
                this.appSettings.mapControlTypeName = value
            }

        public override var defaultMetActivityCode: String
            get() = this.appSettings.defaultMetActivityCode
            set(value) {
                this.appSettings.defaultMetActivityCode = value
            }

        public override var distanceUnitFormatterTypeName: String
            get() = this.appSettings.distanceUnitFormatterTypeName
            set(value) {
                this.appSettings.distanceUnitFormatterTypeName = value
            }

        public override var speedUnitFormatterTypeName: String
            get() = this.appSettings.speedUnitFormatterTypeName
            set(value) {
                this.appSettings.speedUnitFormatterTypeName = value
            }

        public override var energyUnitFormatterTypeName: String
            get() = this.appSettings.energyUnitFormatterTypeName
            set(value) {
                this.appSettings.energyUnitFormatterTypeName = value
            }

        public override var locationProviderTypeName: String
            get() = this.appSettings.locationProviderTypeName
            set(value) {
                this.appSettings.locationProviderTypeName = value
            }

        public override var appUiLocale: String
            get() = this.appSettings.appUiLocale
            set(value) {
                this.appSettings.appUiLocale = value
            }

        public override var enableAutoPauseOnStill: Boolean
            get() = this.appSettings.enableAutoPauseOnStill
            set(value) {
                this.appSettings.enableAutoPauseOnStill = value
            }

        public override var vibrateOnLocationAvailabilityLoss: Boolean
            get() = this.appSettings.vibrateOnLocationAvailabilityLoss
            set(value) {
                this.appSettings.vibrateOnLocationAvailabilityLoss = value
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

        public override var currentDashboardId: UUID?
            get() = this.appSettings.currentDashboardId
            set(value) {
                this.appSettings.currentDashboardId = value
            }

        public override val propertyChanged: Observable<PropertyChangedData> = this.appSettings.propertyChanged

        init {
            val distanceUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_distance_key)
            val energyUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_energy_key)
            val speedUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_speed_key)
            val locationProviderTypeNameKey = context.getString(R.string.appsettings_preference_tracking_location_provider_key)
            val appUiLocaleKey = context.getString(R.string.appsettings_preference_app_ui_locale_key)
            val allowLiveTrackingKey = context.getString(R.string.appsettings_preference_tracking_allow_live_tracking_key)
            val defaultMetActivityCodeKey = context.getString(R.string.appsettings_preference_default_met_activity_code_key)
            val currentTrackRecordingIdKey = IAppSettings::currentTrackRecordingId.name
            val currentDashboardIdKey = IAppSettings::currentDashboardId.name
            val mapControlTypeNameKey = context.getString(R.string.appsettings_preference_map_control_key)
            val enableAutoPauseOnStillkey = context.getString(R.string.appsettings_preference_enable_auto_pause_on_still_key)
            val vibrateOnLocationAvailabilityLossKey = context.getString(R.string.appsettings_preference_notifications_vibrate_on_background_stop_key)

            val currentTrackRecordingId = boundSharedPreferences.getString(currentTrackRecordingIdKey, null)
            if (currentTrackRecordingId != null) {
                this.appSettings.currentTrackRecordingId = UUID.fromString(currentTrackRecordingId)
            }

            val currentDashboardId = boundSharedPreferences.getString(currentDashboardIdKey, null)
            if (currentDashboardId != null) {
                this.appSettings.currentDashboardId = UUID.fromString(currentDashboardId)
            }

            this.propertyChangedSubscription = this.appSettings.propertyChanged
                    .subscribe {
                        if (it.hasChanged) {
                            if (it.propertyName == IAppSettings::currentTrackRecordingId.name || it.propertyName == IAppSettings::currentDashboardId.name) {
                                val sharedPreferenceEditor = boundSharedPreferences.edit()
                                if (it.newValue != null) {
                                    sharedPreferenceEditor.putString(it.propertyName, it.newValue.toString())
                                } else {
                                    sharedPreferenceEditor.remove(it.propertyName)
                                }

                                sharedPreferenceEditor.apply()
                            }
                        }
                    }

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    distanceUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_DISTANCE_CONVERTER_TYPENAME)
                        this.appSettings.distanceUnitFormatterTypeName = currentValue
                    }
                    speedUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_SPEED_CONVERTER_TYPENAME)
                        this.appSettings.speedUnitFormatterTypeName = currentValue
                    }
                    energyUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_ENERGY_CONVERTER_TYPENAME)
                        this.appSettings.energyUnitFormatterTypeName = currentValue
                    }
                    mapControlTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_MAP_CONTROL_TYPENAME)
                        this.appSettings.mapControlTypeName = currentValue
                    }
                    locationProviderTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPENAME)
                        this.appSettings.locationProviderTypeName = currentValue
                    }
                    appUiLocaleKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_APP_UI_LOCALE)
                        this.appSettings.appUiLocale = currentValue
                    }
                    allowLiveTrackingKey -> {
                        val currentValue = boundSharedPreferences.getBoolean(key, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
                        this.appSettings.allowLiveTracking = currentValue
                    }
                    enableAutoPauseOnStillkey -> {
                        val currentValue = boundSharedPreferences.getBoolean(key, AppSettings.DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL)
                        this.appSettings.enableAutoPauseOnStill = currentValue
                    }
                    vibrateOnLocationAvailabilityLossKey -> {
                        val currentValue = boundSharedPreferences.getBoolean(key, AppSettings.DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS)
                        this.appSettings.vibrateOnLocationAvailabilityLoss = currentValue
                    }
                }
            }

            boundSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.appSettings.distanceUnitFormatterTypeName = boundSharedPreferences.getString(distanceUnitFormatterTypeNameKey, AppSettings.DEFAULT_DISTANCE_CONVERTER_TYPENAME)
            this.appSettings.energyUnitFormatterTypeName = boundSharedPreferences.getString(energyUnitFormatterTypeNameKey, AppSettings.DEFAULT_ENERGY_CONVERTER_TYPENAME)
            this.appSettings.speedUnitFormatterTypeName = boundSharedPreferences.getString(speedUnitFormatterTypeNameKey, AppSettings.DEFAULT_SPEED_CONVERTER_TYPENAME)
            this.appSettings.locationProviderTypeName = boundSharedPreferences.getString(locationProviderTypeNameKey, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPENAME)
            this.appSettings.appUiLocale = boundSharedPreferences.getString(appUiLocaleKey, AppSettings.DEFAULT_APP_UI_LOCALE)
            this.appSettings.allowLiveTracking = boundSharedPreferences.getBoolean(allowLiveTrackingKey, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
            this.appSettings.defaultMetActivityCode = boundSharedPreferences.getString(defaultMetActivityCodeKey, AppSettings.DEFAULT_MET_ACTIVITY_CODE)
            this.appSettings.mapControlTypeName = boundSharedPreferences.getString(mapControlTypeNameKey, AppSettings.DEFAULT_MAP_CONTROL_TYPENAME)
            this.appSettings.enableAutoPauseOnStill = boundSharedPreferences.getBoolean(enableAutoPauseOnStillkey, AppSettings.DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL)
            this.appSettings.vibrateOnLocationAvailabilityLoss = boundSharedPreferences.getBoolean(vibrateOnLocationAvailabilityLossKey, AppSettings.DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS)
        }
    }

    companion object {
        public val DEFAULT_MAP_CONTROL_TYPENAME: String = "GoogleTrackRecorderMapFragment"

        public val DEFAULT_LOCATION_PROVIDER_TYPENAME: String = "FusedLocationProvider"

        public const val DEFAULT_ALLOW_LIVE_TRACKING: Boolean = false

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public val DEFAULT_DISTANCE_CONVERTER_TYPENAME: String = "KilometersDistanceConverter"

        public val DEFAULT_ENERGY_CONVERTER_TYPENAME: String = "KilocalorieEnergyConverter"

        public val DEFAULT_SPEED_CONVERTER_TYPENAME: String = "KilometersPerHourSpeedConverter"

        public const val DEFAULT_MET_ACTIVITY_CODE: String = "01015"

        public const val DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL: Boolean = true

        public const val DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS: Boolean = true
    }
}