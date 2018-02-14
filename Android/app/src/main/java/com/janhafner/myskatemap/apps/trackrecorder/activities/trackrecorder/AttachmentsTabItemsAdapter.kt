package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.janhafner.myskatemap.apps.trackrecorder.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal final class AttachmentsTabItemsAdapter(context: Context, @LayoutRes private val itemLayoutId: Int, items: List<Attachment>) : ArrayAdapter<Attachment>(context, itemLayoutId, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

        var kklkl = itemLayout.findViewById<TextView>(R.id.label)
        kklkl.text = this.getItem(position).displayName

        return itemLayout
    }
}

internal final class ObservableAttachmentsItemsAdapter(context: Context, @LayoutRes private val itemLayoutId: Int): ObservableArrayAdapter<Attachment>(context, itemLayoutId) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

        val item = this.getItem(position)

        val icon = itemLayout.findViewById<AppCompatImageView>(R.id.icon)
        // icon.setImageURI(fileUri)

        val label = itemLayout.findViewById<AppCompatTextView>(R.id.label)
        label.text = item.displayName

        return itemLayout
    }
}

internal open class ObservableArrayAdapter<T>(context: Context, @LayoutRes private val itemLayoutId: Int): ArrayAdapter<T>(context, itemLayoutId, ArrayList<T>()) {
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