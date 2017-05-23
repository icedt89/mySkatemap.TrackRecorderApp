import { ILogger } from "../logging/ilogger";
import { Inject } from "@angular/core";
import { Exception } from "../exception";
import { Injectable } from "@angular/core";
import { TrackRecording } from "../track-recording";
import { ITrackUploader } from "./itrack-uploader";

@Injectable()
export class MockedTrackUploader implements ITrackUploader {
    private willFailUpload = false;

    public constructor(@Inject("Logger") private logger: ILogger) {
        this.logger.warn("Using MockedTrackUploader for ITrackUploader");
    }

    public async uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date> {
        this.logger.log("MockedTrackUploader: Track uploaded");

        if (this.willFailUpload) {
            throw new Exception("MockedTrackUploader: Track upload failed");
        }

        return new Date();
    }
}