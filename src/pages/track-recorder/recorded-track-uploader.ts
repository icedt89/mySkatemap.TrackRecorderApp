import "rxjs/Rx";

import { BackgroundGeolocationResponse } from "../../declarations";
import { Http } from "@angular/http";
import { Injectable } from "@angular/core";

@Injectable()
export class RecordedTrackUploader {
    private apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/RecordedTrack";

    public constructor(private http: Http) {
    }

    public uploadRecordedTrack(positions: BackgroundGeolocationResponse[], startedAt: Date): Promise<boolean> {
        const createdRecordedTrackModel = new CreateRecordedTrackModel(startedAt, new Date());
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

        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise().catch(() => {
            return {
                ok: false
            };
        }).then(resolved => resolved.ok);
    }
}

class CreateRecordedTrackModel {
    public constructor(trackingStartedAt: Date, uploadStartedAt: Date) {
        this.TrackingStartedAt = trackingStartedAt.toISOString();
        this.UploadStartedAt = uploadStartedAt.toISOString();
    }

    public TrackingStartedAt: string;

    public UploadStartedAt: string;

    public RecordedPositions: RecordedTrackPositionModel[] = [];
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