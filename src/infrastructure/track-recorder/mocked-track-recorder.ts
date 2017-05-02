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
        const locationSamples: BackgroundGeolocationResponse[] = [];

        for (let i = 0; i < 5; i++) {
            const randomLatitude = Math.random() + 50;
            const randomLongitude = Math.random() + 50;

            const locationSample = <BackgroundGeolocationResponse>{
                latitude: randomLatitude,
                longitude: randomLongitude
            };

            locationSamples.push(locationSample);
        }

        this.positions.push(...locationSamples);

        return Promise.resolve(this.positions);
    }

    public isLocationEnabled(): Promise<boolean> {
        return Promise.resolve(true);
    }

    public showLocationSettings(): void {
    }

    public record(): Promise<void> {
        return Promise.resolve();
    }

    public pause(): Promise<void> {
        return Promise.resolve();
    }

    public deleteAllRecordings(): Promise<void> {
        this.positions = [];

        return Promise.resolve();
    }
}