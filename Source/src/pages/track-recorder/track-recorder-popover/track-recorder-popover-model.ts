import { TrackRecording } from "../../../infrastructure/track-recording";

export class TrackRecorderPopoverModel {
    public constructor(private _currentTrackRecording: TrackRecording | null, private _isPaused: boolean) {
    }

    public get trackRecording(): TrackRecording | null {
        return this._currentTrackRecording;
    }

    public get isPaused(): boolean {
        return this._isPaused;
    }
}