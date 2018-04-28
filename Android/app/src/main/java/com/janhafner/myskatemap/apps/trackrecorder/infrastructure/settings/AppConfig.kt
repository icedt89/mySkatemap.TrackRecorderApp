package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer

internal final class AppConfig : IAppConfig {
    public override val forceUsingOpenStreetMap: Boolean = false

    public override val trackColor: String = "#FFFF3A3C"

    companion object {
        public fun fromAppConfigJson(context: Context, moshi: Moshi): IAppConfig {
            val inputStream = context.resources.openRawResource(R.raw.appconfig)
            val fileContent = inputStream.readBytes()

            inputStream.close()

            val adapter = moshi.adapter<AppConfig>(AppConfig::class.java)

            val buffer = ByteBuffer.wrap(fileContent)
            val json = ByteString.of(buffer).utf8()

            return adapter.fromJson(json)!!
        }
    }
}