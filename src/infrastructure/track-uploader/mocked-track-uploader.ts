import { Exception } from "../exception";
import { Injectable } from "@angular/core";
import { TrackRecording } from "../track-recording";
import { ITrackUploader } from "./itrack-uploader";

@Injectable()
export class MockedTrackUploader implements ITrackUploader {
    private willFailUpload = true;

    public constructor() {
        console.warn("Using MockedTrackUploader for ITrackUploader");
    }

    public async uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        console.log("MockedTrackUploader: Track uploaded");

        if (this.willFailUpload) {
            throw new Exception("MockedTrackUploader: Track upload failed");
        }

        return new Date();
    }
}