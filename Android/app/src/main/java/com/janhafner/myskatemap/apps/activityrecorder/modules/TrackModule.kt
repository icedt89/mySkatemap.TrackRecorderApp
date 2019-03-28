package com.janhafner.myskatemap.apps.activityrecorder.modules

import android.content.Context
import com.couchbase.lite.DatabaseConfiguration
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.*
import com.janhafner.myskatemap.apps.activityrecorder.services.couchdb.CouchDbActivityQueryServiceDataSource
import com.janhafner.myskatemap.apps.activityrecorder.services.couchdb.CouchDbActivityServiceDataSource
import com.janhafner.myskatemap.apps.activityrecorder.services.couchdb.CouchDbFactory
import com.janhafner.myskatemap.apps.activityrecorder.services.couchdb.ICouchDbFactory
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
    public fun provideLocalTrackServiceDataSource(@Named("TrackServiceCouchDbFactory") trackServiceCouchDbFactory: ICouchDbFactory) : IActivityServiceDataSource {
        return CouchDbActivityServiceDataSource(trackServiceCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackService(@Named("LocalTrackServiceDataSource") localActivityServiceDataSource: IActivityServiceDataSource, notifier: INotifier) : IActivityService {
        return ActivityService(localActivityServiceDataSource, notifier)
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
    public fun provideLocalTrackQueryServiceDataSource(@Named("TrackQueryServiceCouchDbFactory") trackQueryServiceCouchDbFactory: ICouchDbFactory) : IActivityQueryServiceDataSource {
        return CouchDbActivityQueryServiceDataSource(trackQueryServiceCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackQueryService(@Named("LocalTrackQueryServiceDataSource") localActivityQueryServiceDataSource: IActivityQueryServiceDataSource, notifier: INotifier) : IActivityQueryService {
        return ActivityQueryService(localActivityQueryServiceDataSource, notifier)
    }
}