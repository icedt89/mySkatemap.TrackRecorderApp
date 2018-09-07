package com.janhafner.myskatemap.apps.trackrecorder.views

import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

internal open class ArrayRecyclerViewAdapter<T>(@LayoutRes private val itemLayoutId: Int)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    protected val items: ArrayList<T> = ArrayList()

    protected val itemViewCreatedSubject: PublishSubject<ItemViewCreatedArgs<View, T>> = PublishSubject.create<ItemViewCreatedArgs<View, T>>()
    public val itemViewCreated: Observable<ItemViewCreatedArgs<View, T>> = this.itemViewCreatedSubject.subscribeOn(Schedulers.computation())

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(this.layoutInflater == null) {
            this.layoutInflater = LayoutInflater.from(parent.context)
        }

        val view = this.layoutInflater!!.inflate(this.itemLayoutId, parent, false)

        return ViewHolder(view)
    }

    public override fun getItemCount(): Int {
        return this.items.count()
    }

    public override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = this.items[position]

        this.onItemViewCreated(holder.itemView, item, position)

        this.itemViewCreatedSubject.onNext(ItemViewCreatedArgs(holder.itemView, item, position))
    }

    protected open fun onItemViewCreated(itemView: View, item: T, position: Int){
    }

    public open fun insert(`object`: T, index: Int) {
        this.items.add(index, `object`)

        this.notifyItemInserted(index)
    }

    public open fun add(`object`: T) {
        this.items.add(`object`)

        this.notifyItemInserted(this.items.count())
    }

    public open fun addAll(collection: MutableCollection<out T>) {
        val currentCount = this.items.count()

        this.items.addAll(collection)

        this.notifyItemRangeInserted(currentCount, collection.count())
    }

    public open fun addAll(vararg items: T) {
        val currentCount = this.items.count()

        this.items.addAll(items)

        this.notifyItemRangeInserted(currentCount, items.count())
    }

    public open fun addAll(list: Iterable<T>) {
        val currentCount = this.items.count()

        this.items.addAll(list)

        this.notifyItemRangeInserted(currentCount, list.count())
    }

    public open fun remove(`object`: T) {
        val index = this.items.indexOf(`object`)

        this.items.remove(`object`)

        this.notifyItemRemoved(index)
    }

    public open fun clear() {
        val currentCount = this.itemCount

        this.items.clear()

        this.notifyItemRangeRemoved(0, currentCount)
    }

    private final class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }
}