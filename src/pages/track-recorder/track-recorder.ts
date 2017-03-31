import { TrackRecorderStateInfo } from "./track-recorder-state-info";
import { BackgroundGeolocation, BackgroundGeolocationConfig } from "../../declarations";
import { Events, Platform } from "ionic-angular";

import { Injectable } from "@angular/core";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";

declare var backgroundGeolocation: BackgroundGeolocation;

@Injectable()
export class TrackRecorder {
    private debug: boolean = false;

    private configuration = <BackgroundGeolocationConfig>{
        desiredAccuracy: 0, // 0 = GPS + Mobile + Wifi + GSM; 10 = Mobile + Wifi + GSM, 100 = Wifi + GSM; 1000 = GSM
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

    public constructor(platform: Platform,
        events: Events) {
        platform.ready().then(() => {
            backgroundGeolocation.configure(null, null, this.configuration);

            backgroundGeolocation.watchLocationMode(enabled => {
                if (this.debug) {
                    console.log(`TrackRecorder: Received change in OS location mode with value: ${<boolean>enabled}`);
                }

                if (!enabled) {
                    this.stop();
                }

                events.publish("TrackRecorder-LocationMode", enabled);
            }, null);
        });
    }

    public get settings(): TrackRecorderSettings {
        const trackRecorderSettings = new TrackRecorderSettings();
        trackRecorderSettings.activitiesInterval = this.configuration.activitiesInterval;
        trackRecorderSettings.desiredAccuracy = this.configuration.desiredAccuracy.toString();
        trackRecorderSettings.distanceFilter = this.configuration.distanceFilter;
        trackRecorderSettings.fastestInterval = this.configuration.fastestInterval;
        trackRecorderSettings.interval = this.configuration.interval;
        trackRecorderSettings.locationProvider = this.configuration.locationProvider.toString();
        trackRecorderSettings.stationaryRadius = this.configuration.stationaryRadius;

        return trackRecorderSettings;
    }

    public setSettings(settings: TrackRecorderSettings): void {
        this.configuration.activitiesInterval = settings.activitiesInterval;
        this.configuration.desiredAccuracy = +settings.desiredAccuracy;
        this.configuration.distanceFilter = settings.distanceFilter;
        this.configuration.fastestInterval = settings.fastestInterval;
        this.configuration.interval = settings.interval;
        this.configuration.locationProvider = +settings.locationProvider;
        this.configuration.stationaryRadius = settings.stationaryRadius;

        this.configuration = Object.assign(this.configuration, settings);

        if (this.debug) {
            console.log(`TrackRecorder: Settings changed: ${JSON.stringify(this.configuration)}`);
        }

        backgroundGeolocation.configure(null, null, this.configuration);
    }

    public getRecorderStateInfo(): Promise<TrackRecorderStateInfo> {
        return new Promise<TrackRecorderStateInfo>((resolve, reject) => {
            backgroundGeolocation.getValidLocations(positions => {
                const result = new TrackRecorderStateInfo();
                if (positions.length) {
                    const lastPosition = positions[positions.length - 1];

                    result.lastLatitude = lastPosition.latitude;
                    result.lastLongitude = lastPosition.longitude;
                    result.recordedPositions = positions;
                }

                resolve(result);
            }, error => reject(error));
        });
    }

    public isLocationEnabled(): Promise<boolean> {
        return new Promise((resolve, reject) => {
            backgroundGeolocation.isLocationEnabled(enabled => resolve(enabled), error => reject(error));
        });
    }

    public showLocationSettings(): void {
        backgroundGeolocation.showLocationSettings();
    }

    public record(): Promise<any> {
        return new Promise((resolve, reject) => {
            backgroundGeolocation.start(() => {
                if (this.debug) {
                    console.log("TrackRecorder: Started");
                }

                resolve(null);
            }, error => reject(error));
        });
    }

    public stop(): Promise<any> {
        return new Promise((resolve, reject) => {
            backgroundGeolocation.stop(() => {
                if (this.debug) {
                    console.log("TrackRecorder: Stopped");
                }

                resolve(null);
            }, error => reject(error));
        });
    }

    public deleteAllRecordings(): Promise<any> {
        return new Promise((resolve, reject) => {
            backgroundGeolocation.deleteAllLocations(() => {
                if (this.debug) {
                    console.log("TrackRecorder: All recordings deleted");
                }

                resolve(null);
            }, error => reject(error));
        });
    }

    public destroy(): void {
        this.stop();

        backgroundGeolocation.stopWatchingLocationMode();
    }

    public debugging(): void {
        this.debug = true;

        console.log("TrackRecorder: Enabled debugging for TrackRecorder");
    }
}