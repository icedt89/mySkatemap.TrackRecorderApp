package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.Sex
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

internal final class UserProfile : IUserProfile {
    private val propertyChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val propertyChanged: Observable<PropertyChangedData> = this.propertyChangedSubject.subscribeOn(Schedulers.computation())

    public override var enableCalculationOfBurnedEnergy: Boolean = false
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfile::enableCalculationOfBurnedEnergy.name, oldValue, value))
        }

    public override var name: String? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfile::name.name, oldValue, value))
        }

    public override var age: Int? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfile::age.name, oldValue, value))
        }

    public override var height: Int? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::height.name, oldValue, value))
        }
    public override var weight: Float? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(::weight.name, oldValue, value))
        }

    public override var sex: Sex? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfile::sex.name, oldValue, value))
        }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences, context: Context) : IUserProfile {
        return UserProfile.SharedPreferencesUserProfileBinding(this, sharedPreferences, context)
    }

    private final class SharedPreferencesUserProfileBinding(private val userProfile: IUserProfile, boundSharedPreferences: SharedPreferences, context: Context) : IUserProfile {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        public override var enableCalculationOfBurnedEnergy: Boolean
            get() = this.userProfile.enableCalculationOfBurnedEnergy
            set(value) {
                this.userProfile.enableCalculationOfBurnedEnergy = value
            }

        public override var name: String?
            get() = this.userProfile.name
            set(value) {
                this.userProfile.name = value
            }

        public override var age: Int?
            get() = this.userProfile.age
            set(value) {
                this.userProfile.age = value
            }

        public override var weight: Float?
            get() = this.userProfile.weight
            set(value) {
                this.userProfile.weight = value
            }

        public override var height: Int?
            get() = this.userProfile.height
            set(value) {
                this.userProfile.height = value
            }

        public override var sex: Sex?
            get() = this.userProfile.sex
            set(value) {
                this.userProfile.sex = value
            }

        public override val propertyChanged: Observable<PropertyChangedData> = this.userProfile.propertyChanged

        init {
            val enableCalculationOfBurnedEnergyKey = context.getString(R.string.userprofilesettings_preference_enablecalculationofburnedenergy_key)
            val nameKey = context.getString(R.string.userprofilesettings_preference_name_key)
            val ageKey = context.getString(R.string.userprofilesettings_preference_age_key)
            val heightKey = context.getString(R.string.userprofilesettings_preference_height_key)
            val weightKey = context.getString(R.string.userprofilesettings_preference_weight_key)
            val sexKey = context.getString(R.string.userprofilesettings_preference_sex_key)

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    enableCalculationOfBurnedEnergyKey -> {
                        val currentValue = boundSharedPreferences.getBoolean(key, UserProfile.DEFAULT_ENABLE_CALCULATION_OF_BURNED_ENERGY)
                        this.userProfile.enableCalculationOfBurnedEnergy = currentValue
                    }
                    nameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, UserProfile.DEFAULT_NAME)
                        this.userProfile.name = currentValue
                    }
                    ageKey -> {
                        val currentValue = boundSharedPreferences.getInt(key, UserProfile.DEFAULT_AGE)
                        this.userProfile.age = currentValue
                    }
                    heightKey -> {
                        val currentValue = boundSharedPreferences.getInt(key, UserProfile.DEFAULT_HEIGHT)
                        this.userProfile.height = currentValue
                    }
                    weightKey -> {
                        val currentValue = boundSharedPreferences.getFloat(key, UserProfile.DEFAULT_WEIGHT)
                        this.userProfile.weight = currentValue
                    }
                    sexKey -> {
                        val currentValue = boundSharedPreferences.getString(key, UserProfile.DEFAULT_SEX.name)
                        this.userProfile.sex = Sex.valueOf(currentValue)
                    }
                }
            }

            boundSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.userProfile.name = boundSharedPreferences.getString(nameKey, UserProfile.DEFAULT_NAME)
            this.userProfile.enableCalculationOfBurnedEnergy = boundSharedPreferences.getBoolean(enableCalculationOfBurnedEnergyKey, UserProfile.DEFAULT_ENABLE_CALCULATION_OF_BURNED_ENERGY)
            this.userProfile.age = boundSharedPreferences.getInt(ageKey, UserProfile.DEFAULT_AGE)
            this.userProfile.height = boundSharedPreferences.getInt(heightKey, UserProfile.DEFAULT_HEIGHT)
            this.userProfile.weight = boundSharedPreferences.getFloat(weightKey, UserProfile.DEFAULT_WEIGHT)

            val sex = boundSharedPreferences.getString(sexKey, UserProfile.DEFAULT_SEX.name)
            this.userProfile.sex = Sex.valueOf(sex)
        }
    }

    companion object {
        public const val DEFAULT_ENABLE_CALCULATION_OF_BURNED_ENERGY: Boolean = false

        public const val DEFAULT_NAME: String = "Anonymous"

        public const val DEFAULT_AGE: Int = 18

        public const val DEFAULT_HEIGHT: Int = 171

        public const val DEFAULT_WEIGHT: Float = 80.0f

        public val DEFAULT_SEX: Sex = Sex.Male
    }
}