package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.couchbase.lite.DatabaseConfiguration
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbTrackQueryServiceDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbTrackServiceDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.ICouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.track.*
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
internal final class TrackModule {
    @Singleton
    @Provides
    @Named("TrackServiceCouchDbFactory")
    public fun provideTrackServiceDataSourceCouchDbFactory(context: Context) : ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(context)

        return CouchDbFactory("tracks", databaseConfiguration)
    }

    @Provides
    @Named("LocalTrackServiceDataSource")
    @Singleton
    public fun provideLocalTrackServiceDataSource(@Named("TrackServiceCouchDbFactory") trackServiceCouchDbFactory: ICouchDbFactory) : ITrackServiceDataSource {
        return CouchDbTrackServiceDataSource(trackServiceCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackService(@Named("LocalTrackServiceDataSource") localTrackServiceDataSource: ITrackServiceDataSource, notifier: INotifier) : ITrackService {
        return TrackService(localTrackServiceDataSource, notifier)
    }

    @Singleton
    @Provides
    @Named("TrackQueryServiceCouchDbFactory")
    public fun provideTrackQueryServiceDataSourceCouchDbFactory(context: Context) : ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(context)

        return CouchDbFactory("tracks-info", databaseConfiguration)
    }

    @Provides
    @Named("LocalTrackQueryServiceDataSource")
    @Singleton
    public fun provideLocalTrackQueryServiceDataSource(@Named("TrackQueryServiceCouchDbFactory") trackQueryServiceCouchDbFactory: ICouchDbFactory) : ITrackQueryServiceDataSource {
        return CouchDbTrackQueryServiceDataSource(trackQueryServiceCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackQueryService(@Named("LocalTrackQueryServiceDataSource") localTrackQueryServiceDataSource: ITrackQueryServiceDataSource, notifier: INotifier) : ITrackQueryService {
        return TrackQueryService(localTrackQueryServiceDataSource, notifier)
    }
}