import { TrackRecording } from "../track-recording";
import { Response } from "@angular/http";
import { BackgroundGeolocationResponse } from "../../declarations";

export interface ITrackUploader {
    uploadRecordedTrack(positions: BackgroundGeolocationResponse[], trackRecording: TrackRecording): Promise<Response>;
}