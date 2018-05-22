package com.janhafner.myskatemap.apps.trackrecorder.services

import com.janhafner.myskatemap.apps.trackrecorder.io.ContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.io.FileSystemFileAccessor
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import org.joda.time.DateTime
import java.io.File

internal final class FileSystemAttachmentHandler(private val trackRecording: TrackRecording,
                                                 private val attachmentDirectoryNavigator: IDirectoryNavigator)
    : IAttachmentHandler{
    public override fun addAttachment(contentInfo: ContentInfo) {
        val fileAccessor = FileSystemFileAccessor.fromUri(contentInfo.uri)

        val destinationFile = fileAccessor.copyTo(this.attachmentDirectoryNavigator)

        val attachment = Attachment(contentInfo.displayName, destinationFile.nativeFile.toURI().toString(), DateTime.now())

        this.trackRecording.attachments.add(attachment)
    }

    public override fun removeAttachment(attachment: Attachment) {
        this.trackRecording.attachments.remove(attachment)

        val fileAccessor = FileSystemFileAccessor(File(attachment.filePath))

        fileAccessor.delete()
    }

    public override fun getAttachments() : List<Attachment> {
        val attachments = this.trackRecording.attachments.toList()
        for (attachment in attachments) {
            if(!this.attachmentDirectoryNavigator.getFile(attachment.displayName).exists()) {
                this.trackRecording.attachments.remove(attachment)
            }
        }

        return this.trackRecording.attachments.toList()
    }
}