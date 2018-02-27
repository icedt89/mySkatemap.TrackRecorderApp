package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.views.ObservableArrayAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal final class ObservableAttachmentsItemsAdapter(context: Context, @LayoutRes private val itemLayoutId: Int): ObservableArrayAdapter<Attachment>(context, itemLayoutId) {
    private val viewHolder: ViewHolder = ViewHolder()

    private val itemViewCreatedSubject: PublishSubject<ItemViewCreatedArgs<View, Attachment>> = PublishSubject.create<ItemViewCreatedArgs<View, Attachment>>()
    public val itemViewCreated: Observable<ItemViewCreatedArgs<View, Attachment>> = this.itemViewCreatedSubject

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var itemLayout = this.viewHolder.tryRetrieve<View>(position)
        if(itemLayout == null) {
            itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

            viewHolder.store(position, itemLayout)
        }

        val item = this.getItem(position)

        this.itemViewCreatedSubject.onNext(ItemViewCreatedArgs<View, Attachment>(itemLayout!!, item, position))

        return itemLayout
    }
}

internal final class ItemViewCreatedArgs<out TView: View, out TItem: Any>(public val view: TView,
                                                                          public val item: TItem,
                                                                          public val position: Int) {
}