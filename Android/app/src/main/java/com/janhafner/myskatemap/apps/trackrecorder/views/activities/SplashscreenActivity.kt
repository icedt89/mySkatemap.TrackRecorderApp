package com.janhafner.myskatemap.apps.trackrecorder.views.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.TemperatureLocationCorrelater
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import org.joda.time.DateTime
import org.joda.time.Period

internal final class SplashscreenActivity: AppCompatActivity() {
    private lateinit var presenter: SplashscreenActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val l1 = Location(1)
        val l2 = Location(2)
        l2.capturedAt = l2.capturedAt.plus(Period.seconds(3))
        val l3 = Location(3)
        l3.capturedAt = l3.capturedAt.plus(Period.seconds(5))
        val l4 = Location(4)
        l4.capturedAt = l4.capturedAt.plus(Period.seconds(8))
        val l5 = Location(5)
        l5.capturedAt = l5.capturedAt.plus(Period.seconds(16))
        val l6 = Location(6)
        l6.capturedAt = l6.capturedAt.plus(Period.seconds(26))
        val l7 = Location(7)
        l7.capturedAt = l7.capturedAt.plus(Period.seconds(30))
        val l8 = Location(8)
        l8.capturedAt = l8.capturedAt.plus(Period.seconds(32))
        val l9 = Location(9)
        l9.capturedAt = l9.capturedAt.plus(Period.seconds(33))
        val l10 = Location(10)
        l10.capturedAt = l10.capturedAt.plus(Period.seconds(40))

        val t1 = Temperature(0.0f) // l1
        val t2 = Temperature(0.0f)
        t2.capturedAt = t2.capturedAt.plus(Period.seconds(3)) // l2
        val t3 = Temperature(0.0f)
        t3.capturedAt = t3.capturedAt.plus(Period.seconds(4)) // NONE
        val t4 = Temperature(0.0f)
        t4.capturedAt = t4.capturedAt.plus(Period.seconds(5)) // l3
        val t5 = Temperature(0.0f)
        t5.capturedAt = t5.capturedAt.plus(Period.seconds(6)) // l4
        val t6 = Temperature(0.0f)
        t6.capturedAt = t6.capturedAt.plus(Period.seconds(20)) // l5
        val t7 = Temperature(0.0f)
        t7.capturedAt = t7.capturedAt.plus(Period.seconds(21)) // l6
        val t8 = Temperature(0.0f)
        t8.capturedAt = t8.capturedAt.plus(Period.seconds(35)) // l8
        val t9 = Temperature(0.0f)
        t9.capturedAt = t9.capturedAt.plus(Period.seconds(38)) // NONE
        val t10 = Temperature(0.0f)
        t10.capturedAt = t10.capturedAt.plus(Period.seconds(44)) // l10

        val correlater = TemperatureLocationCorrelater(Period.seconds(5))
        correlater.correlateAll(listOf(l1, l2, l3, l4, l5, l6, l7, l8, l9 , l10), listOf(t1,t2,t3,t4,t5,t6,t7,t8,t9,t10))

        this.presenter = SplashscreenActivityPresenter(this)
    }
}