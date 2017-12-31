import { TrackRecorderSettings } from "./track-recorder-settings";
import { Observable } from "rxjs/Rx";
import { BackgroundGeolocationResponse } from "@ionic-native/background-geolocation";

export interface ITrackRecorder {
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