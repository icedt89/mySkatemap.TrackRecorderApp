package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

internal open class FileBasedDataStore<T>(private val file: File, private val typeOfT: Type, private val gson: Gson): IFileBasedDataStore<T> {
    @Synchronized
    public final override fun save(data: T) {
        val writer = FileWriter(this.file)

        gson.toJson(data, writer)

        writer.flush()
        writer.close()
    }

    @Synchronized
    public final override fun delete() {
        if (!this.file.exists()) {
            return
        }

        val deleteResult = this.file.delete()

        if (!deleteResult) {
            Log.w("FileBasedDataStore", "File was not properly deleted")
        }
    }

    @Synchronized
    public final override fun getData(): T? {
        if (!this.file.exists()) {
            return null
        }

        val reader = FileReader(this.file)

        val result = gson.fromJson<T>(reader, this.typeOfT)

        reader.close()

        return result as T
    }
}

