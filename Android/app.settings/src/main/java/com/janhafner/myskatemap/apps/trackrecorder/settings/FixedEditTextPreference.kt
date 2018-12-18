package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import android.util.AttributeSet
import com.janhafner.myskatemap.apps.trackrecorder.common.R



public final class FixedEditTextPreference(context: Context, attrs: AttributeSet) : com.takisoft.preferencex.EditTextPreference(context, attrs) {
    private var emptySummaryText: String? = null

    private val initialSummaryText: String

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FixedEditTextPreferenceAttributes, 0, 0)

        this.emptySummaryText = typedArray.getString(R.styleable.FixedEditTextPreferenceAttributes_emptySummary)

        typedArray.recycle()

        this.initialSummaryText = super.getSummary().toString()
    }

    public override fun getSummary(): CharSequence {
        val currentValue = super.getText()
        if(currentValue.isNullOrEmpty()) {
            if(!this.emptySummaryText.isNullOrEmpty()) {
                return this.emptySummaryText!!
            }

            return this.initialSummaryText
        }

        return String.format(this.initialSummaryText, currentValue)
    }

    public fun invalidateSummary() {
        val summary = this.summary

        this.summary = summary
    }
}