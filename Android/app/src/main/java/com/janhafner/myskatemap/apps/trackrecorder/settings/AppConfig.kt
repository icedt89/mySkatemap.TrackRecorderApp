package com.janhafner.myskatemap.apps.trackrecorder.settings

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.squareup.moshi.Moshi
import okio.ByteString
import java.nio.ByteBuffer

internal final class AppConfig : IAppConfig {
    public override val updateBurnedEnergySeconds: Int = 1

    public override val updateStatisticsSeconds: Int = 1

    public override val updateTrackRecordingLocationsSeconds: Int = 1

    public override val updateTrackDistanceSeconds: Int = 5

    public override val updateLiveLocationSession: Int = 5

    public override val forceUsingOpenStreetMap: Boolean = false

    public override val trackColor: String = "#FFFF3A3C"

    public override val useFakeLiveLocationTrackingService: Boolean = true

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