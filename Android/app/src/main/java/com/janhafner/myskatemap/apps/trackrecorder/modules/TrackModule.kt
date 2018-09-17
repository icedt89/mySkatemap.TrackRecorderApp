package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.couchbase.lite.DatabaseConfiguration
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbTracksDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.ICouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITracksDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.track.TrackService
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
internal final class TrackModule {
    @Singleton
    @Provides
    @Named("TracksCouchDbFactory")
    public fun provideTrackDataSourceCouchDbFactory(context: Context) : ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(context)

        return CouchDbFactory("tracks", databaseConfiguration)
    }

    @Provides
    @Named("LocalTracksDataSource")
    @Singleton
    public fun provideLocalTracksDataSource(@Named("TracksCouchDbFactory") tracksCouchDbFactory: ICouchDbFactory) : ITracksDataSource {
        return CouchDbTracksDataSource(tracksCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackService(@Named("LocalTracksDataSource") localTracksDataSource: ITracksDataSource) : ITrackService {
        return TrackService(localTracksDataSource)
    }
}