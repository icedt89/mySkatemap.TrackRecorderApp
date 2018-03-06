package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments

import com.janhafner.myskatemap.apps.trackrecorder.data.Attachment
import io.reactivex.Observable

internal interface IAttachmentsTabFragmentPresenter {
    val attachmentsChanged: Observable<List<Attachment>>

    val attachmentsSelected: Observable<List<Attachment>>

    fun addAttachment(attachment: Attachment)

    fun removeAttachment(attachment: Attachment)

    fun setSelectedAttachments(attachments: List<Attachment>)
}