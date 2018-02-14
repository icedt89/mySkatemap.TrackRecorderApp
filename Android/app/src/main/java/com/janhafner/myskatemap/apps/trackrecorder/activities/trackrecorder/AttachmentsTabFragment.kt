package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.disposables.DisposableContainer

internal final class AttachmentsTabFragment : ListFragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var presenter: ITrackRecorderActivityPresenter

    private val subscriptions: DisposableContainer = CompositeDisposable()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_attachments_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var listAdapter = ObservableAttachmentsItemsAdapter(view.context, R.layout.fragment_attachments_tab_item)

        this.subscriptions.add(listAdapter.subscribeTo(this.presenter.attachmentsChanged))

        this.listAdapter = listAdapter

        val bla1 = view.context.filesDir.absolutePath
        val bla2 = bla1 + "/drawable-hdpi/ic_contacts_white_48dp.png"

        val newAttachment = Attachment("test", bla2)
        this.presenter.addAttachment(newAttachment)
    }

    public override fun setPresenter(presenter: ITrackRecorderActivityPresenter) {
        this.presenter = presenter
    }
}

