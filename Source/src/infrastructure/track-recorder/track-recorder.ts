import { ITrackRecorder } from "./itrack-recorder";
import { Observable, Subject } from "rxjs/Rx";
import { Platform } from "ionic-angular";
import { Injectable } from "@angular/core";
import { TrackRecorderSettings } from "./track-recorder-settings";
import { BackgroundGeolocation, BackgroundGeolocationConfig, BackgroundGeolocationResponse} from "@ionic-native/background-geolocation"

@Injectable()
export class TrackRecorder implements ITrackRecorder {
    private locationModeChangedSubject = new Subject<boolean>();

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

    public constructor(private platform: Platform, private backgroundGeolocation: BackgroundGeolocation) {
        debugger;
        this.initialize();
    }

    private async initialize(): Promise<void> {
        await this.platform.ready();

        this.backgroundGeolocation.configure(this.configuration);

        this.backgroundGeolocation.watchLocationMode().then(enabled => {
            if (!enabled) {
                this.pause();
            }

            this.locationModeChangedSubject.next(enabled);
        })
        .catch(error => this.locationModeChangedSubject.error(error));
    }

    public get locationModeChanged(): Observable<boolean> {
        return this.locationModeChangedSubject;
    }

    public get settings(): TrackRecorderSettings {
        const trackRecorderSettings = new TrackRecorderSettings();
        trackRecorderSettings.desiredAccuracy = this.configuration.desiredAccuracy.toString();
        trackRecorderSettings.distanceFilter = this.configuration.distanceFilter;
        trackRecorderSettings.locationProvider = this.configuration.locationProvider.toString();
        trackRecorderSettings.stationaryRadius = this.configuration.stationaryRadius;

        return trackRecorderSettings;
    }

    public async setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        await this.platform.ready();

        return await new Promise<TrackRecorderSettings>((resolve, reject) => {
            this.configuration.desiredAccuracy = +settings.desiredAccuracy;
            this.configuration.distanceFilter = settings.distanceFilter;
            this.configuration.locationProvider = +settings.locationProvider;
            this.configuration.stationaryRadius = settings.stationaryRadius;

            this.backgroundGeolocation.configure(this.configuration);

            resolve(settings);
        });
    }

    public async getLocations(): Promise<BackgroundGeolocationResponse[]> {
        await this.platform.ready();

        return this.backgroundGeolocation.getValidLocations();
    }

    public async isLocationEnabled(): Promise<boolean> {
        await this.platform.ready();

        return await this.backgroundGeolocation.isLocationEnabled() === 1;
    }

    public async showLocationSettings(): Promise<void> {
        await this.platform.ready();

        this.backgroundGeolocation.showLocationSettings();
    }

    public async record(): Promise<void> {
        await this.platform.ready();

        return this.backgroundGeolocation.start();
    }

    public async pause(): Promise<void> {
        await this.platform.ready();

        return this.backgroundGeolocation.stop();
    }

    public async deleteAllRecordings(): Promise<void> {
        await this.platform.ready();

        return this.backgroundGeolocation.deleteAllLocations();
    }
}