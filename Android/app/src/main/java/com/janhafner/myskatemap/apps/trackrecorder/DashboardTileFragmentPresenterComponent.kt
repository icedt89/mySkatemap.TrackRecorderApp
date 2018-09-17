package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy.BurnedEnergyDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance.DistanceDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.recordingtime.RecordingTimeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface DashboardTileFragmentPresenterComponent {
    fun provideAverageAltitudeDashboardTileFragmentPresenter() : AverageAltitudeDashboardTileFragmentPresenter

    fun provideMinimumAltitudeDashboardTileFragmentPresenter() : MinimumAltitudeDashboardTileFragmentPresenter

    fun provideMaximumAltitudeDashboardTileFragmentPresenter() : MaximumAltitudeDashboardTileFragmentPresenter

    fun provideCurrentAltitudeDashboardTileFragmentPresenter() : CurrentAltitudeDashboardTileFragmentPresenter

    fun provideAverageSpeedDashboardTileFragmentPresenter() : AverageSpeedDashboardTileFragmentPresenter

    fun provideMaximumSpeedDashboardTileFragmentPresenter() : MaximumSpeedDashboardTileFragmentPresenter

    fun provideCurrentSpeedDashboardTileFragmentPresenter() : CurrentSpeedDashboardTileFragmentPresenter

    fun provideDistanceDashboardTileFragmentPresenter() : DistanceDashboardTileFragmentPresenter

    fun provideRecordingTimeDashboardTileFragmentPresenter() : RecordingTimeDashboardTileFragmentPresenter

    fun provideBurnedEnergyDashboardTileFragmentPresenter() : BurnedEnergyDashboardTileFragmentPresenter
}