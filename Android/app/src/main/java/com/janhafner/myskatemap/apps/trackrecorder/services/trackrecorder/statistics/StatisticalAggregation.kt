package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.IObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.statistics.IStatistic
import com.janhafner.myskatemap.apps.trackrecorder.statistics.Statistic
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.Period
import java.util.concurrent.TimeUnit

internal final class StatisticalAggregation(private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                            private val recordingTimeTimer: IObservableTimer,
                                            private val trackDistanceCalculator: ITrackDistanceCalculator,
                                            private val appConfig: IAppConfig) : IStatisticalAggregation {
    public override val burnedEnergyChanged: Observable<BurnedEnergy> = this.burnedEnergyCalculator.calculatedValueChanged

    public override val burnedEnergy: BurnedEnergy
        get() = this.burnedEnergyCalculator.calculatedValue

    public override val recordingTimeChanged: Observable<Period> = this.recordingTimeTimer.secondElapsed

    public override val recordingTime: Period
        get() = this.recordingTimeTimer.secondsElapsed

    public override val trackDistanceChanged: Observable<Float> = this.trackDistanceCalculator.distanceCalculated

    public override val trackDistance: Float
        get() = this.trackDistanceCalculator.distance

    public override val speed: IStatistic = Statistic()

    public override val altitude: IStatistic = Statistic()

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.subscriptions.addAll(
                this.recordingTimeChanged
                        .sample(this.appConfig.updateBurnedEnergySeconds.toLong(), TimeUnit.SECONDS)
                        .subscribe {
                            this.burnedEnergyCalculator.calculate(it.seconds)
                        }
        )
    }

    public override fun addAll(location: List<Location>) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        this.speed.addAll(location.map {
            if(it.speed == null) {
                0.0f
            } else {
                it.speed!!
            }
        })

        this.altitude.addAll(location.map {
            if(it.altitude == null) {
                0.0f
            } else {
                it.altitude!!.toFloat()
            }
        })

        this.trackDistanceCalculator.addAll(location)
    }

    public override fun add(location: Location) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(location.speed == null) {
            this.speed.add(0.0f)
        } else {
            this.speed.add(location.speed!!.toFloat())
        }

        if(location.altitude == null) {
            this.altitude.add(0.0f)
        } else {
            this.altitude.add(location.altitude!!.toFloat())
        }

        this.trackDistanceCalculator.add(location)
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.altitude.destroy()
        this.speed.destroy()

        this.burnedEnergyCalculator.destroy()
        this.recordingTimeTimer.destroy()
        this.trackDistanceCalculator.destroy()

        this.subscriptions.dispose()

        this.isDestroyed = true
    }
}