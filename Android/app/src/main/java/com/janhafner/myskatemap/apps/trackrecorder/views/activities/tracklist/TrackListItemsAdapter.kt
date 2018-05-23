package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.views.ItemViewCreatedArgs
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

internal final class TrackListItemsAdapter : ArrayRecyclerViewAdapter<TrackRecording>(R.layout.activity_track_list_item) {
}

internal open class ArrayRecyclerViewAdapter<T>(@LayoutRes private val itemLayoutId: Int)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    protected val items: ArrayList<T> = ArrayList()

    protected val viewHolderBoundSubject: PublishSubject<ItemViewCreatedArgs<View, T>> = PublishSubject.create<ItemViewCreatedArgs<View, T>>()
    public val viewHolderBound: Observable<ItemViewCreatedArgs<View, T>> = this.viewHolderBoundSubject

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

        this.viewHolderBoundSubject.onNext(ItemViewCreatedArgs(holder.itemView, item, position))
    }

    private final class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    public open fun insert(`object`: T, index: Int) {
        this.items.add(index, `object`)

        this.notifyItemInserted(index)
    }

    public open fun add(`object`: T) {
        this.items.add(`object`)

        this.notifyItemInserted(this.items.count() - 1)
    }

    public open fun addAll(collection: MutableCollection<out T>) {
        val currentCount = this.items.count()

        this.items.addAll(collection)

        this.notifyItemRangeInserted(currentCount, items.count())
    }

    public open fun addAll(vararg items: T) {
        val currentCount = this.items.count()

        this.items.addAll(items)

        this.notifyItemRangeInserted(currentCount, items.count())
    }

    public open fun addAll(list: Iterable<T>) {
        this.items.addAll(list)

        this.notifyDataSetChanged()
    }

    public open fun remove(`object`: T) {
        this.items.remove(`object`)

        this.notifyDataSetChanged()
    }

    public open fun clear() {
        this.items.clear()

        this.notifyDataSetChanged()
    }
}

internal open class ObservableRecyclerViewAdapter<T>(@LayoutRes private val itemLayoutId: Int)
        : ArrayRecyclerViewAdapter<T>(itemLayoutId) {
    public fun subscribeTo(items: Observable<List<T>>): Disposable {
        return items.subscribe{
            this.items.clear()

            this.items.addAll(it)

            this.notifyDataSetChanged()
        }
    }

    public override fun insert(`object`: T, index: Int) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun add(`object`: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun addAll(collection: MutableCollection<out T>) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun addAll(list: Iterable<T>) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun addAll(vararg items: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun remove(`object`: T) {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }

    public override fun clear() {
        throw UnsupportedOperationException("Manipulate observed source to trigger updates!")
    }
}