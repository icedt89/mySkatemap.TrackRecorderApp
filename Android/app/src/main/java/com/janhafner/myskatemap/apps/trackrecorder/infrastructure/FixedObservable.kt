package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import java.util.*

internal class FixedObservable : Observable {
    public constructor() : super();

    public override fun hasChanged() : Boolean {
        return true;
    }
}