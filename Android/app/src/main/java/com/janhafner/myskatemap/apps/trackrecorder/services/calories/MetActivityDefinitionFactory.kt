package com.janhafner.myskatemap.apps.trackrecorder.services.calories

import android.content.Context
import android.support.v4.util.ArrayMap
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer

internal final class MetActivityDefinitionFactory(private val context: Context, private val moshi: Moshi) {
    private val metDefinitions = lazy {
        this.readMetDefinitions()
    }

    public fun preload() {
        this.metDefinitions.value
    }

    private fun readMetDefinitions() : Map<String, Float> {
        val inputStream = context.resources.openRawResource(R.raw.metdefinitions)
        val fileContent = inputStream.readBytes()

        inputStream.close()

        val adapter = moshi.adapter<MetDefinitionFile>(MetDefinitionFile::class.java)

        val buffer = ByteBuffer.wrap(fileContent)
        val json = ByteString.of(buffer).utf8()

        val metDefinitionFile = adapter.fromJson(json)!!

        return metDefinitionFile.definitions
    }

    public fun getMetActivityDefinitionByCode(code: String) : MetActivityDefinition? {
        val fixedCode = code.padStart(5, '0')

        if(!this.metDefinitions.value.containsKey(fixedCode)) {
            return null
        }

        val metValue = this.metDefinitions.value[fixedCode]!!

        return MetActivityDefinition(fixedCode, metValue)
    }

    private final class MetDefinitionFile {
        public val version: String = "1.0"

        public val definitions: Map<String, Float> = ArrayMap<String, Float>()
    }
}