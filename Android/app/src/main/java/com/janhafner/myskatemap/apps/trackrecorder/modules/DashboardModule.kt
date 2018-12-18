package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.couchbase.lite.DatabaseConfiguration
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbDashboardServiceDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.ICouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.DashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardServiceDataSource
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenterFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentPresenterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [DashboardTileFragmentPresenterModule::class])
internal final class DashboardModule {
    @Singleton
    @Provides
    @Named("DashboardServiceCouchDbFactory")
    public fun provideDashboardServiceDataSourceCouchDbFactory(context: Context) : ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(context)

        return CouchDbFactory("dashboards", databaseConfiguration)
    }

    @Provides
    @Named("LocalDashboardServiceDataSource")
    @Singleton
    public fun provideLocalDashboardServiceDataSource(@Named("DashboardServiceCouchDbFactory") trackQueryServiceCouchDbFactory: ICouchDbFactory) : IDashboardServiceDataSource {
        return CouchDbDashboardServiceDataSource(trackQueryServiceCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideDashboardService(@Named("LocalDashboardServiceDataSource") localDashboardServiceDataSource: IDashboardServiceDataSource) : IDashboardService {
        return DashboardService(localDashboardServiceDataSource)
    }

    @Provides
    @Singleton
    public fun provideDashboardTileFragmentPresenterFactory(context: Context) : IDashboardTileFragmentPresenterFactory {
        return DashboardTileFragmentPresenterFactory(context)
    }
}