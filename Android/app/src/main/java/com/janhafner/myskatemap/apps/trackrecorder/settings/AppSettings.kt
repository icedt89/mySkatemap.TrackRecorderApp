package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.KilometersDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.KilometersPerHourSpeedUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*

internal final class AppSettings: IAppSettings {
    private val propertyChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()

    public override val propertyChanged: Observable<PropertyChangedData> = this.propertyChangedSubject

    public override var currentTrackRecordingId: UUID? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::currentTrackRecordingId.name, oldValue, value))
        }

    public override var currentDashboardId: UUID? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::currentDashboardId.name, oldValue, value))
        }

    public override var defaultMetActivityCode: String = AppSettings.DEFAULT_MET_ACTIVITY_CODE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::defaultMetActivityCode.name, oldValue, value))
        }

    public override var allowLiveTracking: Boolean = DEFAULT_ALLOW_LIVE_TRACKING
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::allowLiveTracking.name, oldValue, value))
        }

    public override var appUiLocale: String = DEFAULT_APP_UI_LOCALE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::appUiLocale.name, oldValue, value))
        }

    public override var distanceUnitFormatterTypeName: String = DEFAULT_DISTANCE_UNIT_FORMATTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::distanceUnitFormatterTypeName.name, oldValue, value))
        }

    public override var speedUnitFormatterTypeName: String = DEFAULT_SPEED_UNIT_FORMATTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::speedUnitFormatterTypeName.name, oldValue, value))
        }

    public override var burnedEnergyUnitFormatterTypeName: String = DEFAULT_BURNED_ENERGY_UNIT_FORMATTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::burnedEnergyUnitFormatterTypeName.name, oldValue, value))
        }

    public override var locationProviderTypeName: String = DEFAULT_LOCATION_PROVIDER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::locationProviderTypeName.name, oldValue, value))
        }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences, context: Context) : IAppSettings {
        return SharedPreferencesAppSettingsBinding(this, sharedPreferences, context)
    }

    private final class SharedPreferencesAppSettingsBinding(private val appSettings: IAppSettings, boundSharedPreferences: SharedPreferences, context: Context) : IAppSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        private val propertyChangedSubscription: Disposable

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

        public override var burnedEnergyUnitFormatterTypeName: String
            get() = this.appSettings.burnedEnergyUnitFormatterTypeName
            set(value) {
                this.appSettings.burnedEnergyUnitFormatterTypeName = value
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

        public override val propertyChanged: Observable<PropertyChangedData>
            get() = this.appSettings.propertyChanged

        init {
            val distanceUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_distance_key)
            val burnedEnergyUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_burnedenergy_key)
            val speedUnitFormatterTypeNameKey = context.getString(R.string.appsettings_preference_units_speed_key)
            val locationProviderTypeNameKey = context.getString(R.string.appsettings_preference_tracking_location_provider_key)
            val appUiLocaleKey = "appsettings_preference_app_ui_locale_key" // TODO: Make configurable
            val allowLiveTrackingKey = context.getString(R.string.appsettings_preference_tracking_allow_live_tracking_key)
            val defaultMetActivityCodeKey = context.getString(R.string.appsettings_preference_default_met_activity_code_key)
            val currentTrackRecordingIdKey = context.getString(R.string.appsettings_preference_current_dashboard_id_key)
            val currentDashboardIdKey = context.getString(R.string.appsettings_preference_current_dashboard_id_key)

            val currentTrackRecordingId = boundSharedPreferences.getString(currentTrackRecordingIdKey, null)
            if(currentTrackRecordingId != null) {
                this.appSettings.currentTrackRecordingId = UUID.fromString(currentTrackRecordingId)
            }

            val currentDashboardId = boundSharedPreferences.getString(currentDashboardIdKey, null)
            if(currentDashboardId != null) {
                this.appSettings.currentDashboardId = UUID.fromString(currentDashboardId)
            }

            this.propertyChangedSubscription = this.appSettings.propertyChanged.subscribe {
                if(it.hasChanged && it.propertyName == IAppSettings::currentTrackRecordingId.name) {
                    val sharedPreferenceEditor = boundSharedPreferences.edit()
                    if(it.newValue != null) {
                        sharedPreferenceEditor.putString(it.propertyName, it.newValue.toString())
                    } else {
                        sharedPreferenceEditor.remove(it.propertyName)
                    }

                    sharedPreferenceEditor.apply()
                }
            }

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    distanceUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_DISTANCE_UNIT_FORMATTER_TYPENAME)
                        this.appSettings.distanceUnitFormatterTypeName = currentValue
                    }
                    speedUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_SPEED_UNIT_FORMATTER_TYPENAME)
                        this.appSettings.speedUnitFormatterTypeName = currentValue
                    }
                    burnedEnergyUnitFormatterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_BURNED_ENERGY_UNIT_FORMATTER_TYPENAME)
                        this.appSettings.burnedEnergyUnitFormatterTypeName = currentValue
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
                }
            }

            boundSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.appSettings.distanceUnitFormatterTypeName = boundSharedPreferences.getString(distanceUnitFormatterTypeNameKey, AppSettings.DEFAULT_DISTANCE_UNIT_FORMATTER_TYPENAME)
            this.appSettings.burnedEnergyUnitFormatterTypeName = boundSharedPreferences.getString(burnedEnergyUnitFormatterTypeNameKey, AppSettings.DEFAULT_BURNED_ENERGY_UNIT_FORMATTER_TYPENAME)
            this.appSettings.speedUnitFormatterTypeName = boundSharedPreferences.getString(speedUnitFormatterTypeNameKey, AppSettings.DEFAULT_SPEED_UNIT_FORMATTER_TYPENAME)
            this.appSettings.locationProviderTypeName = boundSharedPreferences.getString(locationProviderTypeNameKey, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPENAME)
            this.appSettings.appUiLocale = boundSharedPreferences.getString(appUiLocaleKey, AppSettings.DEFAULT_APP_UI_LOCALE)
            this.appSettings.allowLiveTracking = boundSharedPreferences.getBoolean(allowLiveTrackingKey, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
            this.appSettings.defaultMetActivityCode = boundSharedPreferences.getString(defaultMetActivityCodeKey, AppSettings.DEFAULT_MET_ACTIVITY_CODE)
        }
    }

    companion object {
        public val DEFAULT_LOCATION_PROVIDER_TYPENAME: String = FusedLocationProvider::class.java.name

        public const val DEFAULT_ALLOW_LIVE_TRACKING: Boolean = false

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public val DEFAULT_DISTANCE_UNIT_FORMATTER_TYPENAME: String = KilometersDistanceUnitFormatter::class.java.name

        public val DEFAULT_BURNED_ENERGY_UNIT_FORMATTER_TYPENAME: String = KilometersDistanceUnitFormatter::class.java.name

        public val DEFAULT_SPEED_UNIT_FORMATTER_TYPENAME: String = KilometersPerHourSpeedUnitFormatter::class.java.name

        public const val DEFAULT_MET_ACTIVITY_CODE: String = "01015"
    }
}