package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import java.io.File

internal final class FileSystemFileAccessor(public override val nativeFile: File): IFileAccessor {
    private val lazyDirectory: Lazy<IDirectoryNavigator> = lazy {
        FileSystemDirectoryNavigator(this.nativeFile.parentFile)
    }

    public override val directory: IDirectoryNavigator
        get() = this.lazyDirectory.value

    public override val name: String
        get() = this.nativeFile.name

    public override fun exists(): Boolean {
        return this.nativeFile.exists()
    }

    public override fun saveContent(byteArray: ByteArray) {
        this.ensureFileExists()

        this.nativeFile.writeBytes(byteArray)
    }

    public override fun getContent(): ByteArray? {
        if(!this.nativeFile.exists()) {
            return null
        }
        return this.nativeFile.readBytes()
    }

    public override fun delete(): IDirectoryNavigator {
        if(this.nativeFile.exists()){
            this.nativeFile.delete()
        }

        return this.directory
    }

    private fun ensureFileExists(){
        if(!this.nativeFile.exists()) {
            this.nativeFile.createNewFile()
        }
    }
}