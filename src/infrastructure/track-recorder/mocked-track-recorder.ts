import { Injectable } from "@angular/core/";
import { BackgroundGeolocationResponse } from "../../declarations";
import { TrackRecorderSettings } from "./track-recorder-settings";
import { Observable, Subject } from "rxjs/Rx";
import { ITrackRecorder } from "./itrack-recorder";

@Injectable()
export class MockedTrackRecorder implements ITrackRecorder {
    private trackRecorderSettings = new TrackRecorderSettings();

    private positions: BackgroundGeolocationResponse[] = [];

    private locationModeChangedSubject = new Subject<boolean>();

    public constructor() {
        console.warn("Using MockedTrackRecorder for ITrackRecorder");
    }

    public get locationModeChanged(): Observable<boolean> {
        return this.locationModeChangedSubject;
    }

    public get settings(): TrackRecorderSettings {
        return this.trackRecorderSettings;
    }

    public async setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        this.trackRecorderSettings = settings;

        console.log("MockedTrackRecorder: Settings updated");

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
        console.log("MockedTrackRecorder: Recording started");

        this.samplePosition();
    }

    public async pause(): Promise<void> {
        console.log("MockedTrackRecorder: Recording paused");

        this.samplePosition();
    }

    public async deleteAllRecordings(): Promise<void> {
        console.log("TrackRecorder: All records deleted");

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

        console.log(`MockedTrackRecorder: Sampling positions complete (${this.positions.length})`);
    }
}