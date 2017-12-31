import { TrackAttachment } from "./track-attachment";
import { BackgroundGeolocationResponse } from "@ionic-native/background-geolocation";

export class TrackRecording {
   /* public static orderByTrackingStartedAtDesc = (a: TrackRecording, b: TrackRecording) => {
        return a._trackingStartedAt    - b._trackingStartedAt
    };
*/
    public static fromLike(trackRecordingLike: TrackRecording): TrackRecording {
        const result = <TrackRecording>Object.assign(new TrackRecording(), trackRecordingLike);
        result._trackAttachments = trackRecordingLike._trackAttachments.map(_ => TrackAttachment.fromLike(_));

        return result;
    }

    private _trackName: string;
    private _trackingStartedAt: Date;
    private _trackingFinishedAt: Date;
    private _trackedPositions: BackgroundGeolocationResponse[] = [];
    private _trackAttachments: TrackAttachment[] = [];

    public get trackName(): string {
        return this._trackName;
    }

    public set trackName(value: string) {
        this._trackName = value;
    }

    public get trackingFinishedAt(): Date {
        return this._trackingFinishedAt;
    }

    public set trackingFinishedAt(value: Date) {
        this._trackingFinishedAt = value;
    }

    public get trackingStartedAt(): Date {
        return this._trackingStartedAt;
    }

    public set trackingStartedAt(value: Date) {
        this._trackingStartedAt = value;
    }

    public get trackedPositions(): BackgroundGeolocationResponse[] {
        return this._trackedPositions;
    }

    public set trackedPositions(value: BackgroundGeolocationResponse[]) {
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

            return TrackAttachment.fromLike(_);
        });
    }

    public get isInvalid(): boolean {
        return !this._trackName;
    }
}