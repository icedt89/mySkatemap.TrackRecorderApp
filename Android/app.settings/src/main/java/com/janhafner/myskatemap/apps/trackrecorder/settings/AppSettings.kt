package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

public final class AppSettings: IAppSettings {
    private val propertyChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val propertyChanged: Observable<PropertyChangedData> = this.propertyChangedSubject.subscribeOn(Schedulers.computation())

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

    public override var defaultMetActivityCode: String = AppSettings.DEFAULT_MET_ACTIVITY_CODE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::defaultMetActivityCode.name, oldValue, value))
        }

    public override var appUiLocale: String = DEFAULT_APP_UI_LOCALE
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::appUiLocale.name, oldValue, value))
        }

    public override var distanceConverterTypeName: String = DEFAULT_DISTANCE_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::distanceConverterTypeName.name, oldValue, value))
        }

    public override var speedConverterTypeName: String = DEFAULT_SPEED_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::speedConverterTypeName.name, oldValue, value))
        }

    public override var energyConverterTypeName: String = DEFAULT_ENERGY_CONVERTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IAppSettings::energyConverterTypeName.name, oldValue, value))
        }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.propertyChangedSubject.onComplete()

        this.isDestroyed = true
    }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences, context: Context): IAppSettings {
        return SharedPreferencesAppSettingsBinding(this, sharedPreferences, context)
    }

    private final class SharedPreferencesAppSettingsBinding(private val appSettings: IAppSettings, private val boundSharedPreferences: SharedPreferences, context: Context) : IAppSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        private val subscriptions: CompositeDisposable = CompositeDisposable()

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

        public override var distanceConverterTypeName: String
            get() = this.appSettings.distanceConverterTypeName
            set(value) {
                this.appSettings.distanceConverterTypeName = value
            }

        public override var speedConverterTypeName: String
            get() = this.appSettings.speedConverterTypeName
            set(value) {
                this.appSettings.speedConverterTypeName = value
            }

        public override var energyConverterTypeName: String
            get() = this.appSettings.energyConverterTypeName
            set(value) {
                this.appSettings.energyConverterTypeName = value
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

        public override val propertyChanged: Observable<PropertyChangedData> = this.appSettings.propertyChanged

        private var isDestroyed: Boolean = false
        public override fun destroy() {
            if (this.isDestroyed) {
                return
            }

            this.boundSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener)

            this.appSettings.destroy()

            this.isDestroyed = true
        }

        init {
            val distanceConverterTypeNameKey = context.getString(R.string.appsettings_preference_units_distance_key)
            val energyConverterTypeNameKey = context.getString(R.string.appsettings_preference_units_energy_key)
            val speedConverterTypeNameKey = context.getString(R.string.appsettings_preference_units_speed_key)
            val appUiLocaleKey = context.getString(R.string.appsettings_preference_app_ui_locale_key)
            val defaultMetActivityCodeKey = context.getString(R.string.appsettings_preference_default_met_activity_code_key)
            val mapControlTypeNameKey = context.getString(R.string.appsettings_preference_map_control_key)
            val enableAutoPauseOnStillKey = context.getString(R.string.appsettings_preference_enable_auto_pause_on_still_key)
            val vibrateOnLocationAvailabilityLossKey = context.getString(R.string.appsettings_preference_notifications_vibrate_on_background_stop_key)

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    distanceConverterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_DISTANCE_CONVERTER_TYPENAME)
                        this.appSettings.distanceConverterTypeName = currentValue
                    }
                    speedConverterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_SPEED_CONVERTER_TYPENAME)
                        this.appSettings.speedConverterTypeName = currentValue
                    }
                    energyConverterTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_ENERGY_CONVERTER_TYPENAME)
                        this.appSettings.energyConverterTypeName = currentValue
                    }
                    mapControlTypeNameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_MAP_CONTROL_TYPENAME)
                        this.appSettings.mapControlTypeName = currentValue
                    }
                    appUiLocaleKey -> {
                        val currentValue = boundSharedPreferences.getString(key, AppSettings.DEFAULT_APP_UI_LOCALE)
                        this.appSettings.appUiLocale = currentValue
                    }
                    enableAutoPauseOnStillKey -> {
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

            this.appSettings.distanceConverterTypeName = boundSharedPreferences.getString(distanceConverterTypeNameKey, AppSettings.DEFAULT_DISTANCE_CONVERTER_TYPENAME)
            this.appSettings.energyConverterTypeName = boundSharedPreferences.getString(energyConverterTypeNameKey, AppSettings.DEFAULT_ENERGY_CONVERTER_TYPENAME)
            this.appSettings.speedConverterTypeName = boundSharedPreferences.getString(speedConverterTypeNameKey, AppSettings.DEFAULT_SPEED_CONVERTER_TYPENAME)
            this.appSettings.appUiLocale = boundSharedPreferences.getString(appUiLocaleKey, AppSettings.DEFAULT_APP_UI_LOCALE)
            this.appSettings.defaultMetActivityCode = boundSharedPreferences.getString(defaultMetActivityCodeKey, AppSettings.DEFAULT_MET_ACTIVITY_CODE)
            this.appSettings.mapControlTypeName = boundSharedPreferences.getString(mapControlTypeNameKey, AppSettings.DEFAULT_MAP_CONTROL_TYPENAME)
            this.appSettings.enableAutoPauseOnStill = boundSharedPreferences.getBoolean(enableAutoPauseOnStillKey, AppSettings.DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL)
            this.appSettings.vibrateOnLocationAvailabilityLoss = boundSharedPreferences.getBoolean(vibrateOnLocationAvailabilityLossKey, AppSettings.DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS)

            this.subscriptions.add(
                this.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .subscribe{
                            when(it.propertyName) {
                                this::enableAutoPauseOnStill.name -> {
                                    this.boundSharedPreferences.edit().putBoolean(enableAutoPauseOnStillKey, it.newValue as Boolean).apply()
                                }
                                /* TODO: this::enableLiveLocation.name -> {
                                    this.boundSharedPreferences.edit().putBoolean(enableLiveLocationKey, it.newValue as Boolean).apply()
                                } */
                            }
                        }
            )
        }
    }

    companion object {
        public const val DEFAULT_MAP_CONTROL_TYPENAME: String = "GoogleTrackRecorderMapFragment"

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public const val DEFAULT_DISTANCE_CONVERTER_TYPENAME: String = "KilometersDistanceConverter"

        public const val DEFAULT_ENERGY_CONVERTER_TYPENAME: String = "KilocalorieEnergyConverter"

        public const val DEFAULT_SPEED_CONVERTER_TYPENAME: String = "KilometersPerHourSpeedConverter"

        public const val DEFAULT_MET_ACTIVITY_CODE: String = "01015"

        public const val DEFAULT_ENABLE_AUTO_PAUSE_ON_STILL: Boolean = true

        public const val DEFAULT_VIBRATE_ON_LOCATION_AVAILABILITY_LOSS: Boolean = true
    }
}