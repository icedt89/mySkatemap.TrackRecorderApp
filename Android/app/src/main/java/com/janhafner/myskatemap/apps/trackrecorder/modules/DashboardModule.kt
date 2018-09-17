package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.DashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardDataSource
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.ReturnAlwaysDefaultDashboardDataSource
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenterFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentPresenterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [DashboardTileFragmentPresenterModule::class])
internal final class DashboardModule {
    @Provides
    @Named("LocalDashboardDataSource")
    @Singleton
    public fun provideLocalDashboardDataSource() : IDashboardDataSource {
        return ReturnAlwaysDefaultDashboardDataSource()
    }

    @Provides
    @Singleton
    public fun provideDashboardService(@Named("LocalDashboardDataSource") localDashboardDataSource: IDashboardDataSource) : IDashboardService {
        return DashboardService(localDashboardDataSource)
    }

    @Provides
    @Singleton
    public fun provideDashboardTileFragmentPresenterFactory(context: Context) : IDashboardTileFragmentPresenterFactory {
        return DashboardTileFragmentPresenterFactory(context)
    }
}