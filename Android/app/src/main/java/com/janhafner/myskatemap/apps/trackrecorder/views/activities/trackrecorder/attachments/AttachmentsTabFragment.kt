package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.getContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.store
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime


internal final class AttachmentsTabFragment : Fragment() {
    private lateinit var presenter: ITrackRecorderActivityPresenter

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val viewHolder: ViewHolder = ViewHolder()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this.setHasOptionsMenu(true)
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_attachments_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewHolder
                .store(view.findViewById<GridView>(R.id.trackrecorderactivity_tab_attachments_grid))
                .store(view.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_tab_attachments_chooseimage_floatingactionbutton))
    }

    public override fun onStart() {
        super.onStart()

        val listAdapter = ObservableAttachmentsItemsAdapter(this.view!!.context, R.layout.fragment_attachments_tab_item)

        val gridView = this.viewHolder.retrieve<GridView>(R.id.trackrecorderactivity_tab_attachments_grid)
        gridView.adapter = listAdapter

        val chooseImageFloatingActionButton = this.viewHolder.retrieve<FloatingActionButton>(R.id.trackrecorderactivity_tab_attachments_chooseimage_floatingactionbutton)

        this.subscriptions.addAll(
                listAdapter.itemViewCreated.subscribe {
                    itemViewCreatedArgs ->
                        val icon = itemViewCreatedArgs.view.findViewById<AppCompatImageView>(R.id.icon)
                        icon.setImageURI(Uri.parse(itemViewCreatedArgs.item.filePath))

                        itemViewCreatedArgs.view.longClicks().subscribe {
                            this.presenter.attachmentsSelected.first(kotlin.collections.emptyList()).subscribe {
                                selectedAttachmemts ->
                                    val o = selectedAttachmemts.toMutableList()

                                    if(selectedAttachmemts.contains(itemViewCreatedArgs.item)) {
                                        o.remove(itemViewCreatedArgs.item)
                                    } else {
                                        o.add(itemViewCreatedArgs.item)
                                    }

                                    presenter.setSelectedAttachments(o)
                            }
                        }
                },

                listAdapter.subscribeTo(this.presenter.attachmentsChanged),

                chooseImageFloatingActionButton.clicks().subscribe {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "image/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)

                    this.startActivityForResult(intent, 1)
                }
        )
    }

    public override fun onStop() {
        super.onStop()

        this.subscriptions.clear()
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.viewHolder.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == 1 && intent != null && intent.data != null) {
            val contentInfo = this.context!!.contentResolver.getContentInfo(intent.data)

            this.presenter.addAttachment(Attachment(contentInfo.displayName, contentInfo.uri.toString(), DateTime.now()))
        }
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(this.activity is TrackRecorderActivity) {
            this.presenter = (this.activity!! as TrackRecorderActivity).presenter
        }
    }
}

