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

    public get ready(): Promise<void> {
        return Promise.resolve();
    }

    public get locationModeChanged(): Observable<boolean> {
        return this.locationModeChangedSubject;
    }

    public get settings(): TrackRecorderSettings {
        return this.trackRecorderSettings;
    }

    public setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings> {
        this.trackRecorderSettings = settings;

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
        this.samplePosition();

        return Promise.resolve();
    }

    public pause(): Promise<void> {
        this.samplePosition();

        return Promise.resolve();
    }

    public deleteAllRecordings(): Promise<void> {
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
    }
}