import { Injectable } from "@angular/core";
import { ResponseOptions } from "@angular/http";
import { TrackRecording } from "../track-recording";
import { Response } from "@angular/http";
import { BackgroundGeolocationResponse } from "../../declarations";
import { ITrackUploader } from "./itrack-uploader";

@Injectable()
export class MockedTrackUploader implements ITrackUploader {
    public uploadRecordedTrack(positions: BackgroundGeolocationResponse[], trackRecording: TrackRecording): Promise<Response> {
        const responseOptions = new ResponseOptions();
        responseOptions.status = 200;

        const response = new Response(responseOptions);

        return Promise.resolve(response);
    }
}