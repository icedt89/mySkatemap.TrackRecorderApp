package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context

internal final class TrackRecorderActivityViewModel {
    private final val context: Context;

    public constructor(context: Context) {
        if(context == null) {
            throw IllegalArgumentException("context");
        }

        this.context = context;
    }
}