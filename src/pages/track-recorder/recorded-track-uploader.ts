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
        createdRecordedTrackModel.RecordedPositions = positions.map(position => {
            const result = new RecordedTrackPositionModel(position.latitude, position.longitude);
            result.Accuracy = position.accuracy;
            result.Bearing = position.bearing;
            result.CapturedAt = position.time;
            result.Speed = position.time;
            
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
        this.TrackingStartedAt= trackingStartedAt.toISOString();
        this.UploadStartedAt= uploadStartedAt.toISOString();
    }

    public TrackingStartedAt: number | string;

    public UploadStartedAt: number | string;

    public RecordedPositions: RecordedTrackPositionModel[] = [];
}

class RecordedTrackPositionModel {
    public constructor(public Latitude: number, public Longitude: number) {
    }

    public Speed: number | null;

    public Accuracy: Number | null;

    public Altitude: Number | null;

    public Bearing: Number | null;

    public CapturedAt: Number | string | null;
}