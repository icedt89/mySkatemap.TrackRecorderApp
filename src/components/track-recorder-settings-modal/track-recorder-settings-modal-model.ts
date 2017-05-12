import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";

export class TrackRecorderSettingsModalModel {
    private _desiredAccuracy: string;
    private _locationProvider: string;
    private _stationaryRadius: number;
    private _recorderDistanceFilter: number;

    public constructor(trackRecorderSettings: TrackRecorderSettings) {
        this._desiredAccuracy = trackRecorderSettings.desiredAccuracy;
        this._locationProvider = trackRecorderSettings.locationProvider;
        this._recorderDistanceFilter = trackRecorderSettings.distanceFilter;
        this._stationaryRadius = trackRecorderSettings.stationaryRadius;
    }

    public get desiredAccuracy(): string {
        return this._desiredAccuracy;
    }

    public set desiredAccuracy(desiredAccuracy: string) {
        this._desiredAccuracy = desiredAccuracy;
    }

    public get stationaryRadius(): number {
        return this._stationaryRadius;
    }

    public set stationaryRadius(stationaryRedius: number) {
        this._stationaryRadius = stationaryRedius;
    }

    public get distanceFilter(): number {
        return this._recorderDistanceFilter;
    }

    public set distanceFilter(distanceFilter: number) {
        this._recorderDistanceFilter = distanceFilter;
    }

    public get locationProvider(): string {
        return this._locationProvider;
    }

    public set locationProvider(locationProvider: string) {
        this._locationProvider = locationProvider;
    }
}