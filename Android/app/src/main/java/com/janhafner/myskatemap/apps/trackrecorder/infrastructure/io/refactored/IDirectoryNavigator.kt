package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.refactored

import java.io.File

internal interface IDirectoryNavigator {
    val parentDirectory: IDirectoryNavigator

    val name: String

    val nativeDirectory: File

    fun exists(): Boolean

    fun delete()

    fun getDirectory(name: String): IDirectoryNavigator

    fun getFile(name: String): IFileAccessor

    fun getFiles(mimeType: String = "*/*"): List<IFileAccessor>

    fun getDirectories(): List<IDirectoryNavigator>
}