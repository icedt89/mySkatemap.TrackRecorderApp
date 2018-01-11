package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import java.util.*

internal final class CurrentTrackRecording {
    public constructor() {
        this.name = "";
        this.trackingStartedAt = DateTime.now();
        this.locations = ArrayList<Location>();
    }

    public final var locations: ArrayList<Location>;

    public final var name: String;

    public final var trackingStartedAt: DateTime;
}