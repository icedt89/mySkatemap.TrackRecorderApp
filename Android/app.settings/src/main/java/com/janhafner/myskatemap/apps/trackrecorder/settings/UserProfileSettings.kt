package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.trackrecorder.core.PropertyChangedData
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Sex
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

public final class UserProfileSettings : IUserProfileSettings {
    private val propertyChangedSubject: PublishSubject<PropertyChangedData> = PublishSubject.create()
    public override val propertyChanged: Observable<PropertyChangedData> = this.propertyChangedSubject

    public override var name: String? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfileSettings::name.name, oldValue, value))
        }

    public override var age: Int? = null
        set(value) {
            val oldValue = field

            field = value

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfileSettings::age.name, oldValue, value))
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

            this.propertyChangedSubject.onNext(PropertyChangedData(IUserProfileSettings::sex.name, oldValue, value))
        }

    public fun bindToSharedPreferences(sharedPreferences: SharedPreferences, context: Context) : IUserProfileSettings {
        return UserProfileSettings.SharedPreferencesUserProfileSettingsBinding(this, sharedPreferences, context)
    }

    private final class SharedPreferencesUserProfileSettingsBinding(private val userProfileSettings: IUserProfileSettings, boundSharedPreferences: SharedPreferences, context: Context) : IUserProfileSettings {
        private val sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

        public override var name: String?
            get() = this.userProfileSettings.name
            set(value) {
                this.userProfileSettings.name = value
            }

        public override var age: Int?
            get() = this.userProfileSettings.age
            set(value) {
                this.userProfileSettings.age = value
            }

        public override var weight: Float?
            get() = this.userProfileSettings.weight
            set(value) {
                this.userProfileSettings.weight = value
            }

        public override var height: Int?
            get() = this.userProfileSettings.height
            set(value) {
                this.userProfileSettings.height = value
            }

        public override var sex: Sex?
            get() = this.userProfileSettings.sex
            set(value) {
                this.userProfileSettings.sex = value
            }

        public override val propertyChanged: Observable<PropertyChangedData> = this.userProfileSettings.propertyChanged

        init {
            val nameKey = context.getString(R.string.userprofilesettings_preference_name_key)
            val ageKey = context.getString(R.string.userprofilesettings_preference_age_key)
            val heightKey = context.getString(R.string.userprofilesettings_preference_height_key)
            val weightKey = context.getString(R.string.userprofilesettings_preference_weight_key)
            val sexKey = context.getString(R.string.userprofilesettings_preference_sex_key)

            this.sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    nameKey -> {
                        val currentValue = boundSharedPreferences.getString(key, UserProfileSettings.DEFAULT_NAME)
                        this.userProfileSettings.name = currentValue
                    }
                    ageKey -> {
                        val currentValue = boundSharedPreferences.getInt(key, UserProfileSettings.DEFAULT_AGE)
                        this.userProfileSettings.age = currentValue
                    }
                    heightKey -> {
                        val currentValue = boundSharedPreferences.getInt(key, UserProfileSettings.DEFAULT_HEIGHT)
                        this.userProfileSettings.height = currentValue
                    }
                    weightKey -> {
                        val currentValue = boundSharedPreferences.getFloat(key, UserProfileSettings.DEFAULT_WEIGHT)
                        this.userProfileSettings.weight = currentValue
                    }
                    sexKey -> {
                        val currentValue = boundSharedPreferences.getString(key, UserProfileSettings.DEFAULT_SEX.name)!!
                        this.userProfileSettings.sex = Sex.valueOf(currentValue)
                    }
                }
            }

            boundSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

            this.userProfileSettings.name = boundSharedPreferences.getString(nameKey, UserProfileSettings.DEFAULT_NAME)
            this.userProfileSettings.age = boundSharedPreferences.getInt(ageKey, UserProfileSettings.DEFAULT_AGE)
            this.userProfileSettings.height = boundSharedPreferences.getInt(heightKey, UserProfileSettings.DEFAULT_HEIGHT)
            this.userProfileSettings.weight = boundSharedPreferences.getFloat(weightKey, UserProfileSettings.DEFAULT_WEIGHT)

            val sex = boundSharedPreferences.getString(sexKey, UserProfileSettings.DEFAULT_SEX.name)!!
            this.userProfileSettings.sex = Sex.valueOf(sex)
        }
    }

    companion object {
        public const val DEFAULT_NAME: String = "Anonymous"

        public const val DEFAULT_AGE: Int = 18

        public const val DEFAULT_HEIGHT: Int = 171

        public const val DEFAULT_WEIGHT: Float = 80.0f

        public val DEFAULT_SEX: Sex = Sex.Male
    }
}