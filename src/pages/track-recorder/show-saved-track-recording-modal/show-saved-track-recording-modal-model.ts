import { TrackRecording } from "../../../infrastructure/track-recording";
import { ArchivedTrackRecording } from "../../../infrastructure/archived-track-recording";
import { TrackAttachment } from "../../../infrastructure/track-attachment";
import { LatLng } from "@ionic-native/google-maps";

export class ShowSavedTrackRecordingModalModel {
    private _trackName: string;
    private _trackingStartedAt: Date;
    private _trackingFinishedAt: Date;
    private _trackedPositions: LatLng[] = [];
    private _trackUploadedAt: Date | null;
    private _numberOfAttachments = 0;
    private _attachments: TrackAttachment[] | null;

    public constructor(trackRecording: ArchivedTrackRecording | TrackRecording) {
        this._trackName = trackRecording.trackName;
        this._trackingStartedAt = trackRecording.trackingStartedAt;
        this._trackingFinishedAt = trackRecording.trackingFinishedAt;

        if (trackRecording instanceof ArchivedTrackRecording) {
            this._trackedPositions = trackRecording.trackedPositions;
            this._numberOfAttachments = trackRecording.numberOfUploadedAttachments;
            this._trackUploadedAt = trackRecording.trackUploadedAt;
        } else {
            this._attachments = trackRecording.trackAttachments;
            this._numberOfAttachments = this._attachments.length;
            this._trackedPositions = trackRecording.trackedPositions.map(_ => new LatLng(_.latitude, _.longitude));
        }
    }

    public get trackedPositions(): LatLng[] {
        return this._trackedPositions;
    }

    public get trackName(): string {
        return this._trackName;
    }

    public get trackingStartedAt(): Date {
        return this._trackingStartedAt;
    }

    public get trackingFinishedAt(): Date {
        return this._trackingFinishedAt;
    }

    public get trackUploadedAt(): Date | null {
        return this._trackUploadedAt;
    }

    public get attachments(): TrackAttachment[] | null {
        return this._attachments;
    }

    public get numberOfAttachments(): number | null {
        return this._numberOfAttachments;
    }
}