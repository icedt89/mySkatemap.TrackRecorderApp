package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class AttachmentsTabFragment : ListFragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var presenter: TrackRecorderActivityPresenter

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_attachments_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = ArrayList<Attachment>()
        items.add(Attachment("gfdgfdgdfg"))
        items.add(Attachment("gfdgfdgdfg3432"))

        this.listAdapter = AttachmentsTabItemsAdapter(view.context, R.layout.fragment_attachments_tab_item, items)
//        val listView = view!!.findViewById<ListViewCompat>(R.id.trackrecorderactivity_fragment_attachments_tab_listview)

//        listView.adapter = AttachmentsTabItemsAdapter(this.context, R.layout.fragment_attachments_tab_item, items)
    }

    public override fun setPresenter(presenter: TrackRecorderActivityPresenter) {
        this.presenter = presenter
    }
}

