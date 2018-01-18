package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.ParameterizedType

internal abstract class FileBasedDataStore<T>(private val file: File) : IDataStore<T> {
    private val gson: Gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, JodaTimeDateTimeGsonAdapter()).create()

    @Throws(IOException::class)
    public final override fun save(data: T) {
        val writer = FileWriter(this.file)

        this.gson.toJson(data, writer)

        writer.flush()
        writer.close()
    }

    public final override fun delete() {
        if (!this.file.exists()) {
            return
        }

        val deleteResult = this.file.delete()

        // TODO: remove if tested!
        if (!deleteResult) {
            Log.w("FileBasedDataStore", "File was not deleted!")
        }
    }

    public final override fun getData(): T? {
        if (!this.file.exists()) {
            return null
        }

        val reader = FileReader(this.file)

        val typeOfT = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

        val result = this.gson.fromJson<T>(reader, typeOfT)

        reader.close()

        return result as T
    }
}