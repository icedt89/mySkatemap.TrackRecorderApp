package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityDependantFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityPresenter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.disposables.DisposableContainer



internal final class AttachmentsTabFragment : Fragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var presenter: ITrackRecorderActivityPresenter

    private val subscriptions: DisposableContainer = CompositeDisposable()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_attachments_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = ObservableAttachmentsItemsAdapter(view.context, R.layout.fragment_attachments_tab_item)

        val subscription = listAdapter.subscribeTo(this.presenter.attachmentsChanged)

        this.subscriptions.add(subscription)

        val gridView = view.findViewById<GridView>(R.id.trackrecorderactivity_tab_attachments_grid)
        gridView.adapter = listAdapter

        val b = view.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_tab_attachments_chooseimage_floatingactionbutton)
        b.clicks().subscribe{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            this.startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && data != null && data.data != null) {
          this.presenter.addAttachment(Attachment("Test", data.data.toString()))
        }
    }

    public override fun setPresenter(presenter: ITrackRecorderActivityPresenter) {
        this.presenter = presenter
    }
}

