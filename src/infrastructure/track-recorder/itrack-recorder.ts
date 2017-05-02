import { BackgroundGeolocationResponse } from "../../declarations";
import { TrackRecorderSettings } from "./track-recorder-settings";
import { Observable } from "rxjs/Rx";

export interface ITrackRecorder {
    ready: Promise<void>;

    locationModeChanged: Observable<boolean>;

    settings: TrackRecorderSettings;

    setSettings(settings: TrackRecorderSettings): Promise<TrackRecorderSettings>;

    getLocations(): Promise<BackgroundGeolocationResponse[]>;

    isLocationEnabled(): Promise<boolean>;

    showLocationSettings(): void;

    record(): Promise<void>;

    pause(): Promise<void>;

    deleteAllRecordings(): Promise<void>;
}