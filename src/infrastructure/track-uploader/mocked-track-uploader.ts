import { Injectable } from "@angular/core";
import { ResponseOptions } from "@angular/http";
import { TrackRecording } from "../track-recording";
import { Response } from "@angular/http";
import { BackgroundGeolocationResponse } from "../../declarations";
import { ITrackUploader } from "./itrack-uploader";

@Injectable()
export class MockedTrackUploader implements ITrackUploader {
    private willFailUpload = true;

    public uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        console.log("MockedTrackUploader: Track uploaded");

        if (this.willFailUpload) {
            return Promise.reject(null);
        }

        return Promise.resolve(new Date());
    }
}