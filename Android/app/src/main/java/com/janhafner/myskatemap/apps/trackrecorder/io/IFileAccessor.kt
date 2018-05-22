package com.janhafner.myskatemap.apps.trackrecorder.io

import java.io.File

internal interface IFileAccessor {
    val directory: IDirectoryNavigator

    val name: String

    val nativeFile: File

    fun exists(): Boolean

    fun saveContent(byteArray: ByteArray)

    fun getContent(): ByteArray?

    fun delete(): IDirectoryNavigator

    fun copyTo(directoryNavigator: IDirectoryNavigator): IFileAccessor
}