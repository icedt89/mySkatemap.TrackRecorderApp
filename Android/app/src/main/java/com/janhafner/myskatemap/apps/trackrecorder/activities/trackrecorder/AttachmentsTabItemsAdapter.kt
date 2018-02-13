package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.janhafner.myskatemap.apps.trackrecorder.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class AttachmentsTabItemsAdapter(context: Context, @LayoutRes private val itemLayoutId: Int, items: ArrayList<Attachment>) : ArrayAdapter<Attachment>(context, itemLayoutId, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var itemLayout = inflater.inflate(this.itemLayoutId, parent, false)

        var kklkl = itemLayout.findViewById<TextView>(R.id.label)
        kklkl.text = this.getItem(position).displayName

        return itemLayout
    }
}