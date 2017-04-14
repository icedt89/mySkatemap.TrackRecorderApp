import { LatLng } from "@ionic-native/google-maps";

export class TrackRecording {
    private _trackName: string;
    private _trackingStartedAt: Date;
    private _trackedPositions: LatLng[] = [];

    public get trackName(): string {
        return this._trackName;
    }

    public set trackName(value: string) {
        this._trackName = value;
    }

    public get trackingStartedAt(): Date {
        return this._trackingStartedAt;
    }

    public set trackingStartedAt(value: Date) {
        this._trackingStartedAt = value;
    }

    public get trackedPositions(): LatLng[] {
        return this._trackedPositions;
    }

    public set trackedPositions(value: LatLng[]) {
        this._trackedPositions = value;
    }
}