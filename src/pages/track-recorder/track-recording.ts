import { TrackAttachment } from "../../infrastructure/track-attachment";
import { LatLng } from "@ionic-native/google-maps";

export class TrackRecording {
    private _trackName: string;
    private _trackingStartedAt: Date;
    private _trackedPositions: LatLng[] = [];
    private _trackAttachments: TrackAttachment[] = [];

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

    public get trackAttachments(): TrackAttachment[] {
        return this._trackAttachments;
    }

    public set trackAttachments(value: TrackAttachment[]) {
        this._trackAttachments = value.map(_ => {
            if (_ instanceof TrackAttachment) {
                return _;
            }

              return Object.assign(new TrackAttachment(), _);
        });
    }
}