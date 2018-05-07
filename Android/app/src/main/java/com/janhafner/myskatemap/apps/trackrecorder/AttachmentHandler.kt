package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.ContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.FileSystemFileAccessor
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import org.joda.time.DateTime
import java.io.File

internal final class AttachmentHandler(private val trackRecording: TrackRecording,
                                       private val attachmentDirectoryNavigator: IDirectoryNavigator) {
    public fun addAttachment(contentInfo: ContentInfo) {
        val fileAccessor = FileSystemFileAccessor.fromUri(contentInfo.uri)

        val destinationFile = fileAccessor.copyTo(this.attachmentDirectoryNavigator)

        val attachment = Attachment(contentInfo.displayName, destinationFile.nativeFile.toURI().toString(), DateTime.now())

        this.trackRecording.attachments.add(attachment)
    }

    public fun removeAttachment(attachment: Attachment) {
        this.trackRecording.attachments.remove(attachment)

        val fileAccessor = FileSystemFileAccessor(File(attachment.filePath))

        fileAccessor.delete()
    }
}