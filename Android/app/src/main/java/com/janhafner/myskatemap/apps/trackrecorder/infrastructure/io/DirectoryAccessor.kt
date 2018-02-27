package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.content.ContentResolver
import android.net.Uri
import android.support.v4.content.MimeTypeFilter
import android.util.Log
import java.io.File
import java.io.FileFilter

internal open class DirectoryAccessor(private val directory: File, private val contentResolver: ContentResolver): IDirectoryAccessor {
    public override fun directory(childDirectoryName: String): IDirectoryAccessor {
        if(!this.directory.exists()) {
            this.directory.mkdir()
        }

        val result = File(this.directory, childDirectoryName)
        if(!result.exists()) {
            result.mkdir()
        }

        return DirectoryAccessor(result, this.contentResolver)
    }

    @Synchronized
    public override fun createNewFile(name: String): File {
        if(!this.directory.exists()) {
            this.directory.mkdir()
        }

        val result = File(this.directory, name)
        if(result.exists()) {
            throw kotlin.io.FileAlreadyExistsException(result)
        }

        result.createNewFile()

        return result
    }

    @Synchronized
    public override fun getFiles(): List<File> {
        if(!this.directory.exists()) {
            this.directory.mkdir()
        }

        return this.getFiles("*/*")
    }

    @Synchronized
    public override fun getFiles(mimeType: String): List<File> {
        if(!this.directory.exists()) {
            this.directory.createNewFile()
        }

        return this.directory.listFiles(FileFilter{
            file ->
                val fileMimeType = this.contentResolver.getType(Uri.fromFile(file))

            MimeTypeFilter.matches(fileMimeType, mimeType)
        }).toList()
    }

    @Synchronized
    public override fun delete() {
        if(!this.directory.exists()) {
            return
        }

        val deleteResult = this.directory.delete()

        if (!deleteResult) {
            Log.w("DirectoryAccessor", "Directory was not properly deleted")
        }
    }

    @Synchronized
    public override fun clear() {
        if(!this.directory.exists()) {
            return
        }

        val contents = this.directory.listFiles()

        contents.map { it.deleteRecursively() }
                .filterNot { it }
                .forEach { Log.w("DirectoryAccessor", "File/Directory was not properly deleted") }
    }
}