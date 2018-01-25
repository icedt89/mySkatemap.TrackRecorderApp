package com.janhafner.myskatemap.apps.trackrecorder.activities

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

internal final class TrackAttachmentsActivityViewModel(private val context: Context) {
    private var subscriptions: CompositeDisposable? = CompositeDisposable()

    init {

    }

    private val canAddPhotoAttachmentChangeSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canAddPhotoAttachmentChange : Observable<Boolean> = this.canAddPhotoAttachmentChangeSubject

    public fun addPhotoAttachment() {

    }

    private val canChooseImageAsAttachmentChangeSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val canChooseImageAsAttachmentChange : Observable<Boolean> = this.canChooseImageAsAttachmentChangeSubject

    public fun chooseImageAsAttachment() {

    }
}