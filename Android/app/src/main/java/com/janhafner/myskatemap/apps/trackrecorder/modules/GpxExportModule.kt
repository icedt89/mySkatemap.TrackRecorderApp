package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.export.gpx.GpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.export.gpx.GpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.export.gpx.IGpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.export.gpx.IGpxTrackWriter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class GpxExportModule {
    @Singleton
    @Provides
    public fun provideGpxFileWriter(context: Context, gpxTrackWriter: IGpxTrackWriter): IGpxFileWriter {
        /*
        val creator = this.applicationContext.getString(R.string.app_name)
        val name = this.applicationContext.getString(R.string.gpx_metadata_name)
        val description = this.applicationContext.getString(R.string.gpx_metadata_description)
        var author = userProfileSettings.name
        if(author == null)
        {
            author = creator
        }

        val gpxData = GpxData()
        gpxData.author = author
        gpxData.creator = creator
        gpxData.name = name
        gpxData.description = description
        */
        return GpxFileWriter(gpxTrackWriter, context)
    }

    @Singleton
    @Provides
    public fun provideGpxTrackWriter(): IGpxTrackWriter {
        return GpxTrackWriter()
    }
}