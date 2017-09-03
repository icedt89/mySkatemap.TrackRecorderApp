import { TrackRecording } from "../track-recording";

export interface ITrackUploader {
    uploadRecordedTrack(trackRecording: TrackRecording): Promise<Date>;
}