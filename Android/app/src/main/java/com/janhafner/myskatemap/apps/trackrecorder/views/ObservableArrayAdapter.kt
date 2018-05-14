package com.janhafner.myskatemap.apps.trackrecorder.views

import android.content.Context
import android.support.annotation.LayoutRes
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

internal open class ObservableArrayAdapter<T>(context: Context, @LayoutRes private val itemLayoutId: Int)
    : ArrayAdapter<T>(context, itemLayoutId, ArrayList<T>()) {
    protected val itemLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    protected val viewCache: MutableMap<Int, View> = ArrayMap<Int, View>()

    protected val itemViewCreatedSubject: PublishSubject<ItemViewCreatedArgs<View, T>> = PublishSubject.create<ItemViewCreatedArgs<View, T>>()
    public val itemViewCreated: Observable<ItemViewCreatedArgs<View, T>> = this.itemViewCreatedSubject

    init {
        this.setNotifyOnChange(false)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var itemLayout = this.viewCache[position]
        if(itemLayout == null) {
            itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

            this.viewCache[position] = itemLayout
        }

        val item = this.getItem(position)

        this.itemViewCreatedSubject.onNext(ItemViewCreatedArgs<View, T>(itemLayout!!, item, position))

        return itemLayout
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