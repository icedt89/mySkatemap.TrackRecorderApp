package com.janhafner.myskatemap.apps.trackrecorder.domain

import android.net.Uri
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackState
import org.joda.time.DateTime
import java.util.*

internal interface IAttachment {
    val displayName: String

    val filePath: Uri

    val attachedAt: DateTime

    var comment: String

    fun rename(displayName: String)

    fun changeComment(comment: String)
}

internal interface IStateChangeEntry {
    val at: DateTime

    val state: TrackState

    fun finish(at: DateTime): IStateChangeEntry

    fun pause(): IStateChangeEntry

    fun resume(): IStateChangeEntry
}

internal interface ICurrentTrackRecordingBase {
    val id: UUID

    val startedAt: DateTime

    val finishedAt: DateTime?
}

internal interface IActiveCurrentTrackRecordind : ICurrentTrackRecordingBase {
    fun finish(): IFinishedCurrentTrackRecording

    fun resume()

    fun pause()

    fun save()
}

internal interface IFinishedCurrentTrackRecording : ICurrentTrackRecordingBase {
}