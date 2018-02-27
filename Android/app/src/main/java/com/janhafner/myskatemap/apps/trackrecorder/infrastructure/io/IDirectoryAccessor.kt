package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import java.io.File

internal interface IDirectoryAccessor {
    fun getFiles(): List<File>

    fun getFiles(mimeType: String): List<File>

    fun createNewFile(name: String): File

    fun delete()

    fun clear()

    fun directory(childDirectoryName: String): IDirectoryAccessor
}