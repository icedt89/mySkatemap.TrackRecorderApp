package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.content.Context
import android.net.Uri
import android.support.annotation.LayoutRes
import android.support.v7.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.views.ObservableArrayAdapter

internal final class ObservableAttachmentsItemsAdapter(context: Context, @LayoutRes private val itemLayoutId: Int): ObservableArrayAdapter<Attachment>(context, itemLayoutId) {
    private val viewHolder: ViewHolder = ViewHolder()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var itemLayout = this.viewHolder.tryRetrieve<View>(position)
        if(itemLayout == null) {
            itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

            viewHolder.store(position, itemLayout)
        }

        val item = this.getItem(position)

        val icon = itemLayout!!.findViewById<AppCompatImageView>(R.id.icon)
        icon.setImageURI(Uri.parse(item.filePath))

        return itemLayout
    }
}