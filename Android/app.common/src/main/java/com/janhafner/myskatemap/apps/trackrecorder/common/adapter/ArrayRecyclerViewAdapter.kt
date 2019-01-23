package com.janhafner.myskatemap.apps.trackrecorder.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

public open class ArrayRecyclerViewAdapter<T>(@LayoutRes private val itemLayoutId: Int)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    protected val items: ArrayList<T> = ArrayList()

    @Deprecated("Experimental")
    public var filterRegex: String? = null

    protected val itemViewCreatedSubject: PublishSubject<ItemViewCreatedArgs<View, T>> = PublishSubject.create<ItemViewCreatedArgs<View, T>>()
    public val itemViewCreated: Observable<ItemViewCreatedArgs<View, T>> = this.itemViewCreatedSubject

    private val arrayChangedSubject: PublishSubject<Unit> = PublishSubject.create()
    public val arrayChanged: Observable<Unit> = this.arrayChangedSubject

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            public override fun onChanged() {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }

            public override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }

            public override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }

            public override  fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }

            public override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }

            public override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                this@ArrayRecyclerViewAdapter.arrayChangedSubject.onNext(Unit)
            }
        })
    }

    public fun findItems(predicate: (item: T) -> Boolean): List<T> {
        return this.items.filter(predicate)
    }

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(this.layoutInflater == null) {
            this.layoutInflater = LayoutInflater.from(parent.context)
        }

        val view = this.layoutInflater!!.inflate(this.itemLayoutId, parent, false)

        return ViewHolder<T>(view)
    }

    public override fun getItemCount(): Int {
        return this.items.count()
    }

    public override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = this.items[position]

        val viewHolder = holder as ViewHolder<T>
        viewHolder.bindItem(item)

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

    private final class ViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        @Deprecated("Experimental")
        public var boundItem: T? = null

        @Deprecated("Experimental")
        public fun bindItem(item: T) {
            this.boundItem = item
        }

        @Deprecated("Experimental")
        public fun show() {
            this.itemView.visibility = VISIBLE
        }

        @Deprecated("Experimental")
        public fun hide() {
            this.itemView.visibility = GONE
        }
    }
}