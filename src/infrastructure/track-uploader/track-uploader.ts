import { ITrackUploader } from "./itrack-uploader";
import { TrackRecording } from "../track-recording";
import { Http } from "@angular/http";
import { Injectable } from "@angular/core";

@Injectable()
export class TrackUploader implements ITrackUploader {
    private apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/TrackRecording";

    public constructor(private http: Http) {
    }

    public uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        const trackUploadedAt = new Date();
        const createdRecordedTrackModel = new CreateRecordedTrackModel(trackRecording.trackName, trackRecording.trackingStartedAt, trackUploadedAt);

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

        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise().then(() => trackUploadedAt);
    }
}

class CreateRecordedTrackModel {
    public constructor(trackName: string, trackingStartedAt: Date, uploadStartedAt: Date) {
        this.TrackName = trackName;
        this.TrackingStartedAt = trackingStartedAt.toISOString();
        this.UploadStartedAt = uploadStartedAt.toISOString();
    }

    public TrackingStartedAt: string;

    public UploadStartedAt: string;

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