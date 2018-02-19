package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson.JodaTimeDateTimeGsonAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson.JodaTimePeriodGsonAdapter
import org.joda.time.DateTime
import org.joda.time.Period
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

internal abstract class FileBasedDataStore<T>(private val file: File, private val typeOfT: Type): IDataStore<T> {
    private val lock: Int = 0

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

    companion object {
        private val gson: Gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, JodaTimeDateTimeGsonAdapter())
                .registerTypeAdapter(Period::class.java, JodaTimePeriodGsonAdapter())
                .create()
    }
}