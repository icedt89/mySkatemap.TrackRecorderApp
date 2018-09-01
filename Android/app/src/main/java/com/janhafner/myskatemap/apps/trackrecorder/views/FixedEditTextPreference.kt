package com.janhafner.myskatemap.apps.trackrecorder.views

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class FixedEditTextPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs) {
    private var emptySummaryText: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FixedEditTextPreferenceAttributes, 0, 0)

        this.emptySummaryText = typedArray.getString(R.styleable.FixedEditTextPreferenceAttributes_emptySummary)

        typedArray.recycle()
    }

    public override fun getSummary(): CharSequence {
        val currentValue = super.getText()
        if(currentValue.isNullOrEmpty()) {
            if(!this.emptySummaryText.isNullOrEmpty()) {
                return this.emptySummaryText!!
            }

            return super.getSummary()
        }

        val summary = super.getSummary().toString()

        return String.format(summary, currentValue)
    }
}