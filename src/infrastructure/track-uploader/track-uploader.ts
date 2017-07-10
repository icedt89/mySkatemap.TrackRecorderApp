import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { ITrackUploader } from "./itrack-uploader";
import { ApiService } from "../api-service";
import { AuthenticationStore } from "../authentication-store";
import { TrackRecording } from "../track-recording";

@Injectable()
export class TrackUploader extends ApiService implements ITrackUploader {
    private resource = "TrackRecording";

    public constructor(http: Http, authenticationStore: AuthenticationStore) {
        super(http, authenticationStore);
    }

    public async uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        const fullUri = super.buildFullUri(`/${this.resource}/Login`);
        const trackUploadedAt = new Date();
        const createdRecordedTrackModel = new CreateRecordedTrackModel(trackRecording.trackName, trackRecording.trackingStartedAt, trackRecording.trackingFinishedAt, trackUploadedAt);

        createdRecordedTrackModel.TrackAttachments = trackRecording.trackAttachments
            .map(trackAttachment => <string>trackAttachment.imageDataUrl)
            .filter(_ => !!_);
        createdRecordedTrackModel.RecordedPositions = trackRecording.trackedPositions.map((position, order) => {
            const result = new RecordedTrackPositionModel(position.latitude, position.longitude, order);

            result.Accuracy = position.accuracy;
            result.Bearing = position.bearing;
            result.CapturedAt = position.time ? new Date(position.time).toISOString() : null;
            result.Speed = position.speed;
            result.Altitude = position.altitude;
            result.ProvidedBy = position.provider;

            return result;
        });

        // TODO: Remove catch clause if api works as expected.
        await super.post(fullUri, createdRecordedTrackModel);

        return trackUploadedAt;
    }
}

class CreateRecordedTrackModel {
    public constructor(trackName: string, trackingStartedAt: Date, trackingFinishedAt: Date, trackUploadedAt: Date) {
        this.TrackName = trackName;
        this.TrackingFinishedAt = trackingFinishedAt.toISOString();
        this.TrackingStartedAt = trackingStartedAt.toISOString();
        this.TrackUploadedAt = trackUploadedAt.toISOString();
    }

    public TrackingStartedAt: string;

    public TrackingFinishedAt: string;

    public TrackUploadedAt: string;

    public TrackName: string;

    public RecordedPositions: RecordedTrackPositionModel[] = [];

    public TrackAttachments: string[] = [];
}

class RecordedTrackPositionModel {
    public constructor(public Latitude: number, public Longitude: number, public Order: number) {
    }

    public Speed: number | null;

    public Accuracy: Number | null;

    public Altitude: Number | null;

    public Bearing: Number | null;

    public CapturedAt: string | null;

    public ProvidedBy: string;
}