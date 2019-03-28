package com.janhafner.myskatemap.apps.activityrecorder.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.Checkable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.janhafner.myskatemap.apps.activityrecorder.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal final class CheckableFloatingActionButton(context: Context?, attrs: AttributeSet?) : FloatingActionButton(context, attrs), Checkable {
    private var isChecked = false

    private var checkedBackgroundTint: ColorStateList? = null

    private var checkedTint: ColorStateList? = null

    private var uncheckedTint: ColorStateList? = null

    private var uncheckedBackgroundTint: ColorStateList? = null

    private val checkable: Boolean

    private val checkedChangedSubject = PublishSubject.create<Boolean>()
    public val checkedChanged: Observable<Boolean> = this.checkedChangedSubject

    init {
        val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.CheckableFloatingActionButton, 0, 0)
        try {
            this.checkable = typedArray.getBoolean(R.styleable.CheckableFloatingActionButton_checkable, false)
            if (this.checkable) {
                this.uncheckedTint = this.supportImageTintList
                this.uncheckedBackgroundTint = this.supportBackgroundTintList

                this.checkedTint = ColorStateList.valueOf(typedArray.getColor(R.styleable.CheckableFloatingActionButton_checkedTint, 0))
                this.checkedBackgroundTint = ColorStateList.valueOf(typedArray.getColor(R.styleable.CheckableFloatingActionButton_checkedBackgroundTint, 0))
            }
        } finally {
            typedArray.recycle()
        }
    }

    override fun performClick(): Boolean {
        this.toggle()

        return super.performClick()
    }

    public override fun toggle() {
        this.setChecked(!this.isChecked)
    }

    public override fun setChecked(checked: Boolean) {
        if(!this.checkable) {
            return
        }

        if (this.isChecked == checked) {
            return
        }

        this.isChecked = checked

        if(this.isChecked) {
            this.imageTintList = this.checkedTint
            this.backgroundTintList = this.checkedBackgroundTint
        } else   {
            this.imageTintList = this.uncheckedTint
            this.backgroundTintList = this.uncheckedBackgroundTint
        }

        this.checkedChangedSubject.onNext(this.isChecked)
    }

    public override fun isChecked(): Boolean {
        return this.isChecked
    }
}