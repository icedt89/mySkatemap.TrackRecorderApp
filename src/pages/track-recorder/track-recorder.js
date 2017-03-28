var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { Events, Platform } from "ionic-angular";
import { Injectable } from "@angular/core";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";
var TrackRecorder = (function () {
    function TrackRecorder(platform, events) {
        var _this = this;
        this.stopped = true;
        this.debug = false;
        this.configuration = {
            desiredAccuracy: 0,
            stationaryRadius: 5,
            distanceFilter: 5,
            // Android only section
            locationProvider: 1,
            interval: 3000,
            fastestInterval: 2000,
            activitiesInterval: 5000,
            startForeground: true,
            // stopOnStillActivity: false,
            notificationTitle: "mySkatemap Streckenerfassung",
            notificationText: "Strecke wird erfasst...",
            notificationIconColor: "#009688"
        };
        platform.ready().then(function () {
            backgroundGeolocation.configure(null, null, _this.configuration);
            backgroundGeolocation.watchLocationMode(function (enabled) {
                if (_this.debug) {
                    console.log("TrackRecorder: Received change in OS location mode with value: " + enabled);
                }
                if (!enabled) {
                    _this.stop();
                }
                events.publish("TrackRecorder-LocationMode", enabled);
            }, null);
        });
    }
    Object.defineProperty(TrackRecorder.prototype, "settings", {
        get: function () {
            var trackRecorderSettings = new TrackRecorderSettings();
            trackRecorderSettings.activitiesInterval = this.configuration.activitiesInterval;
            trackRecorderSettings.desiredAccuracy = this.configuration.desiredAccuracy.toString();
            trackRecorderSettings.distanceFilter = this.configuration.distanceFilter;
            trackRecorderSettings.fastestInterval = this.configuration.fastestInterval;
            trackRecorderSettings.interval = this.configuration.interval;
            trackRecorderSettings.locationProvider = this.configuration.locationProvider.toString();
            trackRecorderSettings.stationaryRadius = this.configuration.stationaryRadius;
            return trackRecorderSettings;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorder.prototype.setSettings = function (settings) {
        this.configuration.activitiesInterval = settings.activitiesInterval;
        this.configuration.desiredAccuracy = +settings.desiredAccuracy;
        this.configuration.distanceFilter = settings.distanceFilter;
        this.configuration.fastestInterval = settings.fastestInterval;
        this.configuration.interval = settings.interval;
        this.configuration.locationProvider = +settings.locationProvider;
        this.configuration.stationaryRadius = settings.stationaryRadius;
        this.configuration = Object.assign(this.configuration, settings);
        if (this.debug) {
            console.log("TrackRecorder: Settings changed: " + JSON.stringify(this.configuration));
        }
        backgroundGeolocation.configure(null, null, this.configuration);
    };
    Object.defineProperty(TrackRecorder.prototype, "lastRecordedLatitude", {
        get: function () {
            return this.lastRecordedPositionLatitude;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorder.prototype, "lastRecordedLongitude", {
        get: function () {
            return this.lastRecordedPositionLongitude;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorder.prototype.getPositions = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            backgroundGeolocation.getValidLocations(function (positions) {
                if (positions.length) {
                    var lastPosition = positions[positions.length - 1];
                    _this.lastRecordedPositionLatitude = lastPosition.latitude;
                    _this.lastRecordedPositionLongitude = lastPosition.longitude;
                }
                resolve(positions);
            }, function (error) { return reject(error); });
        });
    };
    TrackRecorder.prototype.isLocationEnabled = function () {
        return new Promise(function (resolve, reject) {
            backgroundGeolocation.isLocationEnabled(function (enabled) {
                resolve(enabled);
            }, function (error) { return reject(error); });
        });
    };
    Object.defineProperty(TrackRecorder.prototype, "isStopped", {
        get: function () {
            return this.stopped;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorder.prototype.showLocationSettings = function () {
        backgroundGeolocation.showLocationSettings();
    };
    TrackRecorder.prototype.record = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            backgroundGeolocation.start(function () {
                _this.stopped = false;
                if (_this.debug) {
                    console.log("TrackRecorder: Started");
                }
                resolve(null);
            }, function (error) { return reject(error); });
        });
    };
    TrackRecorder.prototype.stop = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            backgroundGeolocation.stop(function () {
                _this.stopped = true;
                if (_this.debug) {
                    console.log("TrackRecorder: Stopped");
                }
                resolve(null);
            }, function (error) { return reject(error); });
        });
    };
    TrackRecorder.prototype.deleteAllRecordings = function () {
        var _this = this;
        return new Promise(function (resolve, reject) {
            backgroundGeolocation.deleteAllLocations(function () {
                _this.lastRecordedPositionLatitude = null;
                _this.lastRecordedPositionLongitude = null;
                if (_this.debug) {
                    console.log("TrackRecorder: All recordings deleted");
                }
                resolve(null);
            }, function (error) { return reject(error); });
        });
    };
    TrackRecorder.prototype.destroy = function () {
        this.stop();
        backgroundGeolocation.stopWatchingLocationMode();
    };
    TrackRecorder.prototype.debugging = function () {
        this.debug = true;
        console.log("TrackRecorder: Enabled debugging for TrackRecorder");
    };
    return TrackRecorder;
}());
TrackRecorder = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [Platform,
        Events])
], TrackRecorder);
export { TrackRecorder };
//# sourceMappingURL=track-recorder.js.map