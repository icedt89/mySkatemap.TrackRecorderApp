package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import org.joda.time.Period
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.ParameterizedType

internal abstract class FileBasedDataStore<T>(private val file: File): IDataStore<T> {


    public final override fun save(data: T) {
        val writer = FileWriter(this.file)

        FileBasedDataStore.gson.toJson(data, writer)

        writer.flush()
        writer.close()
    }

    public final override fun delete() {
        if (!this.file.exists()) {
            return
        }

        val deleteResult = this.file.delete()

        if (!deleteResult) {
            Log.w("FileBasedDataStore", "File was not properly deleted")
        }
    }

    public final override fun getData(): T? {
        if (!this.file.exists()) {
            return null
        }

        val reader = FileReader(this.file)

        // Horrible hack to get type of T...
        val typeOfT = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

        val result = FileBasedDataStore.gson.fromJson<T>(reader, typeOfT)

        reader.close()

        return result as T
    }

    companion object {
        private val gson: Gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, JodaTimeDateTimeGsonAdapter())
                .registerTypeAdapter(Period::class.java, JodaTimePeriodGsonAdapter())
                .create()
    }
}