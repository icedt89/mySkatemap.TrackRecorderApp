package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.Sex
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.KilometersTrackDistanceUnitFormatter
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

    public override var defaultMetActivityCode: String = AppSettings.DEFAULT_MET_ACTIVITY_CODE
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("defaultMetActivityCode", oldValue, value))
        }

    public override var enableFitnessActivityTracking: Boolean = DEFAULT_ENABLE_FITNESS_ACTIVITY_TRACKING
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("enableFitnessActivityTracking", oldValue, value))
        }
    public override var userAge: Int = DEFAULT_USER_AGE
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("userAge", oldValue, value))
        }
    public override var userWeightInKilograms: Float = DEFAULT_USER_WEIGHT_IN_KILOGRAMS
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("userWeightInKilograms", oldValue, value))
        }
    public override var userHeightInCentimeters: Float = DEFAULT_USER_HEIGHT_IN_CENTIMETERS
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("userHeightInCentimeters", oldValue, value))
        }
    public override var userSex: Sex = DEFAULT_USER_SEX
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("userSex", oldValue, value))
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

    public override var trackDistanceUnitFormatterTypeName: String = DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("trackDistanceUnitFormatterTypeName", oldValue, value))
        }

    public override var locationProviderTypeName: String = DEFAULT_LOCATION_PROVIDER_TYPENAME
        set(value) {
            val oldValue = field

            field = value

            this.appSettingsChangedSubject.onNext(PropertyChangedData("locationProviderTypeName", oldValue, value))
        }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences) : IAppSettings {
        return SharedPreferencesAppSettingsBinding(this, sharedPreferences)
    }

    private final class SharedPreferencesAppSettingsBinding(private val appSettings: IAppSettings, boundSharedPreferences: SharedPreferences) : IAppSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        private val appSettingsChangedSubscription: Disposable

        public override var defaultMetActivityCode: String
            get() = this.appSettings.defaultMetActivityCode
            set(value) {
                this.appSettings.defaultMetActivityCode = value
            }

        public override var enableFitnessActivityTracking: Boolean
            get() = this.appSettings.enableFitnessActivityTracking
            set(value) {
                this.appSettings.enableFitnessActivityTracking = value
            }

        public override var userAge: Int
            get() = this.appSettings.userAge
            set(value) {
                this.appSettings.userAge = value
            }

        public override var userWeightInKilograms: Float
            get() = this.appSettings.userWeightInKilograms
            set(value) {
                this.appSettings.userWeightInKilograms = value
            }

        public override var userHeightInCentimeters: Float
            get() = this.appSettings.userHeightInCentimeters
            set(value) {
                this.appSettings.userHeightInCentimeters = value
            }

        public override var userSex: Sex
            get() = this.appSettings.userSex
            set(value) {
                this.appSettings.userSex = value
            }

        public override var trackDistanceUnitFormatterTypeName: String
            get() = this.appSettings.trackDistanceUnitFormatterTypeName
            set(value) {
                this.appSettings.trackDistanceUnitFormatterTypeName = value
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

        public override val appSettingsChanged: Observable<PropertyChangedData>
            get() = this.appSettings.appSettingsChanged

        init {
            val currentTrackRecordingIdKey = "currentTrackRecordingId"
            val currentTrackRecordingId = boundSharedPreferences.getString(currentTrackRecordingIdKey, null)
            if(currentTrackRecordingId != null) {
                this.appSettings.currentTrackRecordingId = UUID.fromString(currentTrackRecordingId)
            }

            this.appSettingsChangedSubscription = this.appSettings.appSettingsChanged.subscribe {
                if(it.hasChanged && it.propertyName == currentTrackRecordingIdKey) {
                    val sharedPreferenceEditor = boundSharedPreferences.edit()
                    if(it.newValue != null) {
                        sharedPreferenceEditor.putString(it.propertyName, it.newValue.toString())
                    } else {
                        sharedPreferenceEditor.remove(it.propertyName)
                    }

                    sharedPreferenceEditor.apply()
                }
            }

            // TODO: Extract keys into local val's
            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                when(key) {
                    "preference_units_distance" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPENAME)
                        this.appSettings.trackDistanceUnitFormatterTypeName = currentValue
                    }
                    "preference_tracking_location_provider" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_LOCATION_PROVIDER_TYPENAME)
                        this.appSettings.locationProviderTypeName = currentValue
                    }
                    "preference_app_ui_locale" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_APP_UI_LOCALE)
                        this.appSettings.appUiLocale = currentValue
                    }
                    "preference_tracking_allow_live_tracking" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
                        this.appSettings.allowLiveTracking = currentValue
                    }
                    "preference_fitness_enable_fitness_activity_tracking" -> {
                        val currentValue = sharedPreferences.getBoolean(key, AppSettings.DEFAULT_ENABLE_FITNESS_ACTIVITY_TRACKING)
                        this.appSettings.enableFitnessActivityTracking = currentValue
                    }
                    "preference_fitness_user_sex" -> {
                        val currentValue = sharedPreferences.getString(key, AppSettings.DEFAULT_USER_SEX.toString())
                        this.appSettings.userSex = Sex.valueOf(currentValue)
                    }
                }
            }

            boundSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.appSettings.trackDistanceUnitFormatterTypeName = boundSharedPreferences.getString("preference_units_distance", AppSettings.DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPENAME)
            this.appSettings.locationProviderTypeName = boundSharedPreferences.getString("preference_tracking_location_provider", AppSettings.DEFAULT_LOCATION_PROVIDER_TYPENAME)
            this.appSettings.appUiLocale = boundSharedPreferences.getString("preference_app_ui_locale", AppSettings.DEFAULT_APP_UI_LOCALE)
            this.appSettings.allowLiveTracking = boundSharedPreferences.getBoolean("preference_tracking_allow_live_tracking", AppSettings.DEFAULT_ALLOW_LIVE_TRACKING)
            this.appSettings.enableFitnessActivityTracking = boundSharedPreferences.getBoolean("preference_fitness_enable_fitness_activity_tracking", AppSettings.DEFAULT_ENABLE_FITNESS_ACTIVITY_TRACKING)
            this.appSettings.userSex = Sex.valueOf(boundSharedPreferences.getString("preference_fitness_user_sex", AppSettings.DEFAULT_USER_SEX.toString()))
            this.appSettings.userAge = boundSharedPreferences.getString("preference_fitness_user_age", AppSettings.DEFAULT_USER_AGE.toString()).toInt()
            this.appSettings.userHeightInCentimeters = boundSharedPreferences.getString("preference_fitness_user_height_in_centimeters", AppSettings.DEFAULT_USER_HEIGHT_IN_CENTIMETERS.toString()).toFloat()
            this.appSettings.userWeightInKilograms = boundSharedPreferences.getString("preference_fitness_user_weight_in_kilograms", AppSettings.DEFAULT_USER_WEIGHT_IN_KILOGRAMS.toString()).toFloat()
            this.appSettings.defaultMetActivityCode = boundSharedPreferences.getString("preference_fitness_default_met_activity_code", AppSettings.DEFAULT_MET_ACTIVITY_CODE)
        }
    }

    companion object {
        public val DEFAULT_LOCATION_PROVIDER_TYPENAME: String = FusedLocationProvider::class.java.name

        public const val DEFAULT_ALLOW_LIVE_TRACKING: Boolean = false

        public val DEFAULT_APP_UI_LOCALE: String = Locale.getDefault().language

        public val DEFAULT_TRACK_DISTANCE_UNIT_FORMATTER_TYPENAME: String = KilometersTrackDistanceUnitFormatter::class.java.name

        public const val DEFAULT_ENABLE_FITNESS_ACTIVITY_TRACKING: Boolean = false

        public const val DEFAULT_MET_ACTIVITY_CODE: String = "01015"

        public const val DEFAULT_USER_AGE: Int = 18

        public const val DEFAULT_USER_WEIGHT_IN_KILOGRAMS: Float = 0.0f

        public const val DEFAULT_USER_HEIGHT_IN_CENTIMETERS: Float = 0.0f

        public val DEFAULT_USER_SEX: Sex = Sex.Male
    }
}