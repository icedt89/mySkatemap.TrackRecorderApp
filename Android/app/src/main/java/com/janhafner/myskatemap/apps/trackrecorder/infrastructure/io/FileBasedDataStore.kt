package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

internal open class FileBasedDataStore<T>(private val file: File, private val typeOfT: Type, moshi: Moshi): IFileBasedDataStore<T> {
    private val adapter: JsonAdapter<T> = moshi.adapter<T>(this.typeOfT)

    @Synchronized
    public final override fun save(data: T) {
        val writer = FileWriter(this.file)

        val jsonData = this.adapter.toJson(data)
        writer.write(jsonData)

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

        val content =reader.readText()
        val result = this.adapter.fromJson(content)

        reader.close()

        return result
    }
}

