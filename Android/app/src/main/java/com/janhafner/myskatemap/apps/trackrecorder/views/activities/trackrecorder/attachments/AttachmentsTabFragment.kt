package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.longClicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.getContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_attachments_tab.*


internal final class AttachmentsTabFragment : Fragment() {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this.setHasOptionsMenu(true)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(this.activity is INeedFragmentVisibilityInfo) {
            (this.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this, isVisibleToUser)
        }
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_attachments_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)
    }

    public override fun onStart() {
        super.onStart()

        val listAdapter = AttachmentItemsAdapter(this.view!!.context)

        this.trackrecorderactivity_tab_attachments_grid.adapter = listAdapter

        this.subscriptions.addAll(
                listAdapter.itemViewCreated.subscribe {
                    itemViewCreatedArgs ->
                        val icon = itemViewCreatedArgs.view.findViewById<AppCompatImageView>(R.id.icon)
                        icon.setImageURI(Uri.parse(itemViewCreatedArgs.item.filePath))

                        itemViewCreatedArgs.view.longClicks().subscribe {
                            /*this.presenter.attachmentsSelected.last(kotlin.collections.emptyList()).subscribe {
                                selectedAttachmemts ->
                                    val o = selectedAttachmemts.toMutableList()

                                    if(selectedAttachmemts.contains(itemViewCreatedArgs.item)) {
                                        o.remove(itemViewCreatedArgs.item)
                                    } else {
                                        o.add(itemViewCreatedArgs.item)
                                    }

                                    presenter.setSelectedAttachments(o)
                            }*/
                        }
                }

                // listAdapter.subscribeTo(this.presenter.attachmentsChanged),
        )
    }

    public override fun onStop() {
        super.onStop()

        this.subscriptions.clear()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == 1 && intent != null && intent.data != null) {
            val contentInfo = this.context!!.contentResolver.getContentInfo(intent.data)

            //this.presenter.addAttachment(Attachment(contentInfo.displayName, contentInfo.uri.toString(), DateTime.now()))
        }
    }
}

