package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.ApplicationComponent
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter

internal final class DashboardTileFragmentPresenterFactory(context: Context) : IDashboardTileFragmentPresenterFactory {
    private val injector: ApplicationComponent = context.getApplicationInjector()

    public override fun createPresenterFromTypeName(typeName: String): DashboardTileFragmentPresenter {
        when(typeName) {
            AverageAltitudeDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideAverageAltitudeDashboardTileFragmentPresenter()
            MaximumAltitudeDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideMaximumAltitudeDashboardTileFragmentPresenter()
            MinimumAltitudeDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideMinimumAltitudeDashboardTileFragmentPresenter()
            CurrentAltitudeDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideCurrentAltitudeDashboardTileFragmentPresenter()
            CurrentSpeedDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideCurrentSpeedDashboardTileFragmentPresenter()
            MaximumSpeedDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideMaximumSpeedDashboardTileFragmentPresenter()
            AverageSpeedDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideAverageSpeedDashboardTileFragmentPresenter()
            BurnedEnergyDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideBurnedEnergyDashboardTileFragmentPresenter()
            DistanceDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideDistanceDashboardTileFragmentPresenter()
            NumberOfLocationsDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideNumberOfLocationsDashboardTileFragmentPresenter()
            RecordingTimeDashboardTileFragmentPresenter::class.java.simpleName ->
                return this.injector.provideRecordingTimeDashboardTileFragmentPresenter()
        }

        throw IllegalArgumentException("Unknown ${typeName} supplied!")
    }
}