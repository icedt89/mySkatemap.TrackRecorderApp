package com.janhafner.myskatemap.apps.trackrecorder.services

import com.janhafner.myskatemap.apps.trackrecorder.io.ContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Attachment

internal interface IAttachmentHandler {
    fun addAttachment(contentInfo: ContentInfo)

    fun removeAttachment(attachment: Attachment)

    fun getAttachments() : List<Attachment>
}