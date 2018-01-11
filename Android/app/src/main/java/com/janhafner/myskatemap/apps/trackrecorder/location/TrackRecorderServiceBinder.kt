package com.janhafner.myskatemap.apps.trackrecorder.location

import android.os.Binder

internal final class TrackRecorderServiceBinder : Binder {
    public constructor(service: ITrackRecorderService) {
        if (service == null) {
            throw IllegalArgumentException("service");
        }

        this.service = service;
    }

    public final val service: ITrackRecorderService;
}