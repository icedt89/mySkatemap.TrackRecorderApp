import "rxjs/Rx";

import { BackgroundGeolocationResponse } from "../../declarations";
import { Http } from "@angular/http";
import { Injectable } from "@angular/core";

@Injectable()
export class RecordedTrackUploader {
    private apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/RecordedTrack";

    public constructor(private http: Http) {
    }

    public uploadRecordedTrack(positions: BackgroundGeolocationResponse[]): Promise<boolean> {
        const createdRecordedTrackModel = new CreateRecordedTrackModel();
        createdRecordedTrackModel.RecordedPositions = positions.map(position => new RecordedTrackPositionModel(position.latitude, position.longitude));

        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise().catch(() => {
            return {
                ok: false
            }
        }).then(resolved => resolved.ok);
    }
}

class CreateRecordedTrackModel {
    public RecordedPositions: RecordedTrackPositionModel[] = [];
}

class RecordedTrackPositionModel {
    public constructor(public Latitude: number, public Longitude: number) {
    }
}