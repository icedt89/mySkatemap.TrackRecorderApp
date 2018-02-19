package com.janhafner.myskatemap.apps.trackrecorder.views

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal open class ObservableArrayAdapter<T>(context: Context, @LayoutRes private val itemLayoutId: Int)
    : ArrayAdapter<T>(context, itemLayoutId, ArrayList<T>()) {
    protected val itemLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        this.setNotifyOnChange(false)
    }

    public fun subscribeTo(items: Observable<List<T>>): Disposable {
        return items.subscribe{
            super.clear()

            super.addAll(it)

            this.notifyDataSetChanged()
        }
    }

    override fun insert(`object`: T, index: Int) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    override fun add(`object`: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    override fun addAll(collection: MutableCollection<out T>?) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    override fun addAll(vararg items: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    override fun remove(`object`: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    override fun clear() {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }
}