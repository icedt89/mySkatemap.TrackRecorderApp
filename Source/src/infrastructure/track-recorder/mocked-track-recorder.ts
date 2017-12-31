import { Inject } from "@angular/core";
import { ILogger } from "../logging/ilogger";
import { Injectable } from "@angular/core/";
import { TrackRecorderSettings } from "./track-recorder-settings";
import { Observable, Subject } from "rxjs/Rx";
import { ITrackRecorder } from "./itrack-recorder";
import { BackgroundGeolocationResponse } from "@ionic-native/background-geolocation";

@Injectable()
export class MockedTrackRecorder implements ITrackRecorder {
    private trackRecorderSettings = new TrackRecorderSettings();

    private positions: BackgroundGeolocationResponse[] = [];

    private locationModeChangedSubject = new Subject<boolean>();

    public constructor(@Inject("Logger") private logger: ILogger) {
        this.logger.warn("Using MockedTrackRecorder for ITrackRecorder");
    }

    public get locationModeChanged(): Observable<boolean> {
        return this.locationModeChangedSubject;
    }

    public get settings(): TrackRecorderSettings {
        return this.trackRecorderSettings;
    }

    public async setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        this.trackRecorderSettings = settings;

        this.logger.log("MockedTrackRecorder: Settings updated");

        return this.trackRecorderSettings;
    }

    public async getLocations(): Promise<BackgroundGeolocationResponse[]> {
        this.samplePosition();

        return this.positions.map(_ => _);
    }

    public async isLocationEnabled(): Promise<boolean> {
        return true;
    }

    public showLocationSettings(): void {
    }

    public async record(): Promise<void> {
        this.logger.log("MockedTrackRecorder: Recording started");

        this.samplePosition();
    }

    public async pause(): Promise<void> {
        this.logger.log("MockedTrackRecorder: Recording paused");

        this.samplePosition();
    }

    public async deleteAllRecordings(): Promise<void> {
        this.logger.log("TrackRecorder: All records deleted");

        this.positions = [];
    }

    private samplePosition(): void {
        const locationSamples: BackgroundGeolocationResponse[] = [];

        for (let i = 1; i <= 5; i++) {
            const randomLatitude = (Math.random() * i) + (50 + i);
            const randomLongitude = (Math.random() * i) + (50 + i);

            const locationSample = <BackgroundGeolocationResponse>{
                latitude: randomLatitude,
                longitude: randomLongitude
            };

            locationSamples.push(locationSample);
        }

        this.positions.push(...locationSamples);

        this.logger.log(`MockedTrackRecorder: Sampling positions complete (${this.positions.length})`);
    }
}