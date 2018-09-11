package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.SharedPreferences

public final class FixedTypeConversionSharedPreferencesAdapter(private val sharedPreferences: SharedPreferences) : SharedPreferences {
    public override fun contains(key: String?): Boolean {
        return this.sharedPreferences.contains(key)
    }

    public override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return this.sharedPreferences.getBoolean(key, defValue)
    }

    public override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    public override fun getInt(key: String?, defValue: Int): Int {
        val rawValue = this.sharedPreferences.getString(key, defValue.toString())
        if(rawValue.isNullOrEmpty()) {
            return defValue
        }

        return rawValue.toInt()
    }

    public override fun getAll(): MutableMap<String, *> {
        return this.sharedPreferences.all
    }

    public override fun edit(): SharedPreferences.Editor {
        return this.sharedPreferences.edit()
    }

    public override fun getLong(key: String?, defValue: Long): Long {
        val rawValue = this.sharedPreferences.getString(key, defValue.toString())
        if(rawValue.isNullOrEmpty()) {
            return defValue
        }

        return rawValue.toLong()
    }

    public override fun getFloat(key: String?, defValue: Float): Float {
        val rawValue = this.sharedPreferences.getString(key, defValue.toString())
        if(rawValue.isNullOrEmpty()) {
            return defValue
        }

        return rawValue.toFloat()
    }

    public override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return this.sharedPreferences.getStringSet(key, defValues)
    }

    public override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    public override fun getString(key: String?, defValue: String?): String? {
        return this.sharedPreferences.getString(key, defValue)
    }
}