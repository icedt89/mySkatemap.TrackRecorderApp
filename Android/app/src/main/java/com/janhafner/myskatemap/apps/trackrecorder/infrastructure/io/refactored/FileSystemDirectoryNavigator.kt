package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.refactored

import java.io.File

internal final class FileSystemDirectoryNavigator(public override val nativeDirectory: File): IDirectoryNavigator {
    public override val parentDirectory: IDirectoryNavigator

    init {
        this.ensureDirectoryExists()

        this.parentDirectory = FileSystemDirectoryNavigator(this.nativeDirectory.parentFile)
    }

    public override val name: String
        get() = this.nativeDirectory.name

    public override fun exists(): Boolean {
        return this.nativeDirectory.exists()
    }

    public override fun delete() {
        if(this.nativeDirectory.exists()) {
            this.nativeDirectory.deleteRecursively()
        }
    }

    public override fun getDirectory(name: String): IDirectoryNavigator {
        val directory = File(this.nativeDirectory, name)

        return FileSystemDirectoryNavigator(directory)
    }

    public override fun getFile(name: String): IFileAccessor {
        val file = File(this.nativeDirectory, name)

        return FileSystemFileAccessor(file)
    }

    public override fun getFiles(mimeType: String): List<IFileAccessor> {
        if(!this.nativeDirectory.exists()){
            return emptyList()
        }

        val files = this.nativeDirectory.listFiles {
            pathname ->
                pathname.isFile
        }

        return files.map {
            FileSystemFileAccessor(it)
        }
    }

    public override fun getDirectories(): List<IDirectoryNavigator> {
        if(!this.nativeDirectory.exists()){
            return emptyList()
        }

        val files = this.nativeDirectory.listFiles {
            pathname ->
                !pathname.isFile
        }

        return files.map {
            FileSystemDirectoryNavigator(it)
        }
    }

    private fun ensureDirectoryExists(){
        if(!this.nativeDirectory.exists()) {
            this.nativeDirectory.mkdirs()
        }
    }

    companion object {
        public fun baseDirectory(basePath: String): IDirectoryNavigator {
            val directory = File(basePath)

            return FileSystemDirectoryNavigator(directory)
        }
    }
}