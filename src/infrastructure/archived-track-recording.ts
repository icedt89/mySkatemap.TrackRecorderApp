import { TrackRecording } from "./track-recording";
import { LatLng } from "@ionic-native/google-maps";

export class ArchivedTrackRecording {
    public static fromLike(archivedTrackRecordingLike: ArchivedTrackRecording): ArchivedTrackRecording {
        return Object.assign(new ArchivedTrackRecording(), archivedTrackRecordingLike);
    }

    public static fromTrackRecording(trackRecording: TrackRecording, trackUploadedAt: Date): ArchivedTrackRecording {
        const result = new ArchivedTrackRecording();

        result._trackName = trackRecording.trackName;
        result._numberOfUploadedAttachments = trackRecording.trackAttachments.length;
        result._trackedPositions = trackRecording.trackedPositions.map(position => new LatLng(position.latitude, position.longitude));
        result._trackingStartedAt = trackRecording.trackingStartedAt;
        result._trackingFinishedAt = trackRecording.trackingFinishedAt;
        result._trackUploadedAt = trackUploadedAt;

        return result;
    }

    private _trackName: string;
    private _trackingStartedAt: Date;
    private _trackingFinishedAt: Date;
    private _trackedPositions: LatLng[] = [];
    private _trackUploadedAt: Date;
    private _numberOfUploadedAttachments: number;

    public get numberOfUploadedAttachments(): number {
        return this._numberOfUploadedAttachments;
    }

    public get trackingStartedAt(): Date {
        return this._trackingStartedAt;
    }

    public get trackingFinishedAt(): Date {
        return this._trackingFinishedAt;
    }

    public get trackUploadedAt(): Date {
        return this._trackUploadedAt;
    }

    public get trackedPositions(): LatLng[] {
        return this._trackedPositions;
    }

    public get trackName(): string {
        return this._trackName;
    }
}