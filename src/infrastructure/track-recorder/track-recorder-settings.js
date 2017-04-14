var TrackRecorderSettings = (function () {
    function TrackRecorderSettings() {
    }
    Object.defineProperty(TrackRecorderSettings.prototype, "desiredAccuracy", {
        get: function () {
            return this._desiredAccuracy;
        },
        set: function (desiredAccuracy) {
            this._desiredAccuracy = desiredAccuracy;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "stationaryRadius", {
        get: function () {
            return this._stationaryRadius;
        },
        set: function (stationaryRedius) {
            this._stationaryRadius = stationaryRedius;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "distanceFilter", {
        get: function () {
            return this._recorderDistanceFilter;
        },
        set: function (distanceFilter) {
            this._recorderDistanceFilter = distanceFilter;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderSettings.prototype, "locationProvider", {
        get: function () {
            return this._locationProvider;
        },
        set: function (locationProvider) {
            this._locationProvider = locationProvider;
        },
        enumerable: true,
        configurable: true
    });
    return TrackRecorderSettings;
}());
export { TrackRecorderSettings };
//# sourceMappingURL=track-recorder-settings.js.map