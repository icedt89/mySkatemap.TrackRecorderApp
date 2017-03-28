var TrackRecorderSettings = (function () {
    function TrackRecorderSettings() {
    }
    Object.defineProperty(TrackRecorderSettings.prototype, "desiredAccuracy", {
        get: function () {
            return this.recorderDesiredAccuracy;
        },
        set: function (desiredAccuracy) {
            this.recorderDesiredAccuracy = desiredAccuracy;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "stationaryRadius", {
        get: function () {
            return this.recorderStationaryRadius;
        },
        set: function (stationaryRedius) {
            this.recorderStationaryRadius = stationaryRedius;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "distanceFilter", {
        get: function () {
            return this.recorderDistanceFilter;
        },
        set: function (distanceFilter) {
            this.recorderDistanceFilter = distanceFilter;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "locationProvider", {
        get: function () {
            return this.recorderLocationProvider;
        },
        set: function (locationProvider) {
            this.recorderLocationProvider = locationProvider;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "interval", {
        get: function () {
            return this.recorderInterval;
        },
        set: function (interval) {
            this.recorderInterval = interval;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "fastestInterval", {
        get: function () {
            return this.recorderFastestInterval;
        },
        set: function (fastestInterval) {
            this.recorderFastestInterval = fastestInterval;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "activitiesInterval", {
        get: function () {
            return this.recorderActivitiesInterval;
        },
        set: function (activitiesInterval) {
            this.recorderActivitiesInterval = activitiesInterval;
        },
        enumerable: true,
        configurable: true
    });
    return TrackRecorderSettings;
}());
export { TrackRecorderSettings };
//# sourceMappingURL=track-recorder-settings.js.map