var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { Subject } from "rxjs/Rx";
import { Platform } from "ionic-angular";
import { Injectable } from "@angular/core";
import { TrackRecorderSettings } from "./track-recorder-settings";
var TrackRecorder = (function () {
    function TrackRecorder(platform) {
        var _this = this;
        this.locationModeChangedSubject = new Subject();
        this._ready = new Promise(function (resolve) { return _this.readyResolve = resolve; });
        this.configuration = {
            desiredAccuracy: 0,
            stationaryRadius: 5,
            distanceFilter: 5,
            // Android only section
            locationProvider: 0,
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
                if (!enabled) {
                    _this.stop();
                }
                _this.locationModeChangedSubject.next(enabled);
            }, function (error) { return _this.locationModeChangedSubject.error(error); });
        }).then(function () { return _this.readyResolve(); });
    }
    Object.defineProperty(TrackRecorder.prototype, "ready", {
        get: function () {
            return this._ready;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorder.prototype, "locationModeChanged", {
        get: function () {
            return this.locationModeChangedSubject;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorder.prototype, "settings", {
        get: function () {
            var trackRecorderSettings = new TrackRecorderSettings();
            trackRecorderSettings.desiredAccuracy = this.configuration.desiredAccuracy.toString();
            trackRecorderSettings.distanceFilter = this.configuration.distanceFilter;
            trackRecorderSettings.locationProvider = this.configuration.locationProvider.toString();
            trackRecorderSettings.stationaryRadius = this.configuration.stationaryRadius;
            return trackRecorderSettings;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorder.prototype.setSettings = function (settings) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            _this.configuration.desiredAccuracy = +settings.desiredAccuracy;
            _this.configuration.distanceFilter = settings.distanceFilter;
            _this.configuration.locationProvider = +settings.locationProvider;
            _this.configuration.stationaryRadius = settings.stationaryRadius;
            backgroundGeolocation.configure(null, function (error) { return reject(error); }, _this.configuration);
            resolve(settings);
        });
    };
    TrackRecorder.prototype.getLocations = function () {
        return new Promise(function (resolve, reject) { return backgroundGeolocation.getValidLocations(function (positions) { return resolve(positions); }, function (error) { return reject(error); }); });
    };
    TrackRecorder.prototype.isLocationEnabled = function () {
        return new Promise(function (resolve, reject) { return backgroundGeolocation.isLocationEnabled(function (enabled) { return resolve(enabled); }, function (error) { return reject(error); }); });
    };
    TrackRecorder.prototype.showLocationSettings = function () {
        backgroundGeolocation.showLocationSettings();
    };
    TrackRecorder.prototype.record = function () {
        return new Promise(function (resolve, reject) { return backgroundGeolocation.start(function () { return resolve(); }, function (error) { return reject(error); }); });
    };
    TrackRecorder.prototype.stop = function () {
        return new Promise(function (resolve, reject) { return backgroundGeolocation.stop(function () { return resolve(); }, function (error) { return reject(error); }); });
    };
    TrackRecorder.prototype.deleteAllRecordings = function () {
        return new Promise(function (resolve, reject) { return backgroundGeolocation.deleteAllLocations(function () { return resolve(); }, function (error) { return reject(error); }); });
    };
    TrackRecorder.prototype.destroy = function () {
        this.stop();
        backgroundGeolocation.stopWatchingLocationMode();
    };
    return TrackRecorder;
}());
TrackRecorder = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [Platform])
], TrackRecorder);
export { TrackRecorder };
//# sourceMappingURL=track-recorder.js.map