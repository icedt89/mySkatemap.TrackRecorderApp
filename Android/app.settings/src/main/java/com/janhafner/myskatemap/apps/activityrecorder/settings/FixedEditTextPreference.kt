package com.janhafner.myskatemap.apps.activityrecorder.settings

import android.content.Context
import android.util.AttributeSet



public final class FixedEditTextPreference(context: Context, attrs: AttributeSet) : com.takisoft.preferencex.EditTextPreference(context, attrs) {
    private var emptySummaryText: String? = null

    private val initialSummaryText: String

    init {
        val typedArray = context.obtainStyledAttributes(attrs, com.janhafner.myskatemap.apps.activityrecorder.settings.R.styleable.FixedEditTextPreference, 0, 0)
        try {
            this.emptySummaryText = typedArray.getString(com.janhafner.myskatemap.apps.activityrecorder.settings.R.styleable.FixedEditTextPreference_emptySummary)

            this.initialSummaryText = super.getSummary().toString()
        } finally {
            typedArray.recycle()
        }
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