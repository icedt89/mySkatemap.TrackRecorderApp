package com.janhafner.myskatemap.apps.activityrecorder.core.adapter

import android.content.Context
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

public open class DynamicArrayAdapter<T>(context: Context, @LayoutRes private val itemLayoutId: Int)
    : ArrayAdapter<T>(context, itemLayoutId, ArrayList<T>()) {
    protected val itemLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    protected val viewCache: MutableMap<Int, View> = ArrayMap<Int, View>()

    protected val itemViewCreatedSubject: PublishSubject<ItemViewCreatedArgs<View, T>> = PublishSubject.create<ItemViewCreatedArgs<View, T>>()
    public val itemViewCreated: Observable<ItemViewCreatedArgs<View, T>> = this.itemViewCreatedSubject

    init {
        this.setNotifyOnChange(false)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemLayout = this.viewCache[position]
        if(itemLayout == null) {
            itemLayout = this.itemLayoutInflater.inflate(this.itemLayoutId, parent, false)

            this.viewCache[position] = itemLayout
        }

        val item = this.getItem(position)!!

        this.onItemViewCreated(itemLayout!!, item, position)

        this.itemViewCreatedSubject.onNext(ItemViewCreatedArgs<View, T>(itemLayout, item, position))

        return itemLayout
    }

    protected open fun onItemViewCreated(itemView: View, item: T, position: Int){
    }
}