package com.janhafner.myskatemap.apps.trackrecorder.views

import android.content.Context
import android.support.annotation.LayoutRes
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding2.widget.dataChanges
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

@Deprecated("Rename to something more generic")
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
        var itemLayout = this.viewCache[position]
        if(itemLayout == null) {
            itemLayout = this.itemLayoutInflater.inflate(this.itemLayoutId, parent, false)

            this.viewCache[position] = itemLayout
        }

        val item = this.getItem(position)

        this.onItemViewCreated(itemLayout!!, item, position)

        this.itemViewCreatedSubject.onNext(ItemViewCreatedArgs<View, T>(itemLayout, item, position))

        return itemLayout
    }

    protected open fun onItemViewCreated(itemView: View, item: T, position: Int){
    }
}