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

    public setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        this.trackRecorderSettings = settings;

        console.log("MockedTrackRecorder: Settings updated");

        return Promise.resolve(this.trackRecorderSettings);
    }

    public getLocations(): Promise<BackgroundGeolocationResponse[]> {
        this.samplePosition();

        return Promise.resolve(this.positions.map(_ => _));
    }

    public isLocationEnabled(): Promise<boolean> {
        return Promise.resolve(true);
    }

    public showLocationSettings(): void {
    }

    public record(): Promise<void> {
        console.log("MockedTrackRecorder: Recording started");

        this.samplePosition();

        return Promise.resolve();
    }

    public pause(): Promise<void> {
        console.log("MockedTrackRecorder: Recording paused");

        this.samplePosition();

        return Promise.resolve();
    }

    public deleteAllRecordings(): Promise<void> {
        console.log("TrackRecorder: All records deleted");

        this.positions = [];

        return Promise.resolve();
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