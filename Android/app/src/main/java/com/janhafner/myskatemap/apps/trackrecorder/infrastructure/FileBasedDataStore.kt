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

internal abstract class FileBasedDataStore<T> : IDataStore<T> {
    private final val file: File;

    private final val LOGTAG: String;

    private final val gson: Gson;

    protected constructor(file: File){
        if (file == null) {
            throw IllegalArgumentException("file");
        }

        this.file = file
        this.gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, JodaTimeGsonAdapter()).create();
        this.LOGTAG = this.javaClass.simpleName;
    }

    @Throws(IOException::class)
    public final override fun save(data: T) {
        if (data == null) {
            throw IllegalArgumentException("data");
        }

        val writer = FileWriter(this.file);

        this.gson.toJson(data, writer);

        writer.flush();
        writer.close();
    }

    public final override fun delete() {
        if (this.file.exists()) {
            val deleteResult = this.file.delete();

            // TODO: remove if tested!
            if (!deleteResult) {
                Log.w(this.LOGTAG, String.format("File was not deleted!"))
            }
        }
    }

    public final override fun getData(): T? {
        if (this.file.exists()) {
            try {
                val reader = FileReader(this.file);

                val typeOfT = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0];

                val result = this.gson.fromJson<T>(reader, typeOfT);

                reader.close();

                return result as T;
            } catch (exception: IOException) {
                Log.w(this.LOGTAG, String.format("IOException during data retrieval. \$s", exception))
            }
        }

        return null
    }
}