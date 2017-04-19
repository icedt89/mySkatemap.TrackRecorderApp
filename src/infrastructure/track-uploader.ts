import { TrackRecording } from "../pages/track-recorder/track-recording";
import "rxjs/Rx";

import { BackgroundGeolocationResponse } from "../declarations";
import { Http, Response } from "@angular/http";
import { Injectable } from "@angular/core";

@Injectable()
export class TrackUploader {
    private apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/TrackRecording";

    public constructor(private http: Http) {
    }

    public uploadRecordedTrack(positions: BackgroundGeolocationResponse[], trackRecording: TrackRecording): Promise<Response> {
        const createdRecordedTrackModel = new CreateRecordedTrackModel(trackRecording.trackName, trackRecording.trackingStartedAt, new Date());

        createdRecordedTrackModel.TrackAttachments = trackRecording.trackAttachments.map(trackAttachment => trackAttachment.imageDataUrl);
        createdRecordedTrackModel.RecordedPositions = positions.map((position, order) => {
            const result = new RecordedTrackPositionModel(position.latitude, position.longitude, order);
            result.Accuracy = position.accuracy;
            result.Bearing = position.bearing;
            result.CapturedAt = position.time ? new Date(position.time).toISOString() : null;
            result.Speed = position.speed;
            result.Altitude = position.altitude;
            result.ProvidedBy = position.provider;

            return result;
        });

        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise();
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