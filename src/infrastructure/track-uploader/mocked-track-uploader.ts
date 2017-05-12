import { Injectable } from "@angular/core";
import { TrackRecording } from "../track-recording";
import { ITrackUploader } from "./itrack-uploader";

@Injectable()
export class MockedTrackUploader implements ITrackUploader {
    private willFailUpload = true;

    public constructor() {
        console.warn("Using MockedTrackUploader for ITrackUploader");
    }

    public uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        console.log("MockedTrackUploader: Track uploaded");

        if (this.willFailUpload) {
            return Promise.reject(null);
        }

        return Promise.resolve(new Date());
    }
}