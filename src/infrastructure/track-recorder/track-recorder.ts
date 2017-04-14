import { BackgroundGeolocation } from "../../declarations";
import { Events, Platform } from "ionic-angular";

import { Injectable } from "@angular/core";
import { TrackRecorderSettings } from "./track-recorder-settings";

declare var backgroundGeolocation: BackgroundGeolocation.BackgroundGeolocation;

@Injectable()
export class TrackRecorder {
    private configuration = <BackgroundGeolocation.BackgroundGeolocationConfig>{
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
                if (!enabled) {
                    this.stop();
                }

                events.publish("TrackRecorder-LocationMode", enabled);
            }, null);
        });
    }

    public get settings(): TrackRecorderSettings {
        const trackRecorderSettings = new TrackRecorderSettings();
        trackRecorderSettings.desiredAccuracy = this.configuration.desiredAccuracy.toString();
        trackRecorderSettings.distanceFilter = this.configuration.distanceFilter;
        trackRecorderSettings.locationProvider = this.configuration.locationProvider.toString();
        trackRecorderSettings.stationaryRadius = this.configuration.stationaryRadius;

        return trackRecorderSettings;
    }

    public setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        return new Promise((resolve, reject) => {
            this.configuration.desiredAccuracy = +settings.desiredAccuracy;
            this.configuration.distanceFilter = settings.distanceFilter;
            this.configuration.locationProvider = +settings.locationProvider;
            this.configuration.stationaryRadius = settings.stationaryRadius;

            backgroundGeolocation.configure(null, error => reject(error), this.configuration);

            resolve(settings);
        });
    }

    public getLocations(): Promise<BackgroundGeolocation.BackgroundGeolocationResponse[]> {
        return new Promise<BackgroundGeolocation.BackgroundGeolocationResponse[]>((resolve, reject) => backgroundGeolocation.getValidLocations(positions => resolve(positions), error => reject(error)));
    }

    public isLocationEnabled(): Promise<boolean> {
        return new Promise((resolve, reject) => backgroundGeolocation.isLocationEnabled(enabled => resolve(enabled), error => reject(error)));
    }

    public showLocationSettings(): void {
        backgroundGeolocation.showLocationSettings();
    }

    public record(): Promise<any> {
        return new Promise((resolve, reject) => backgroundGeolocation.start(() => resolve(null), error => reject(error)));
    }

    public stop(): Promise<any> {
        return new Promise((resolve, reject) => backgroundGeolocation.stop(() => resolve(null), error => reject(error)));
    }

    public deleteAllRecordings(): Promise<any> {
        return new Promise((resolve, reject) => backgroundGeolocation.deleteAllLocations(() => resolve(null), error => reject(error)));
    }

    public destroy(): void {
        this.stop();

        backgroundGeolocation.stopWatchingLocationMode();
    }
}