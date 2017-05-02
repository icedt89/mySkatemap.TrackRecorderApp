import { TrackRecording } from "../../../infrastructure/track-recording";

export class TrackRecorderPopoverModel {
    public constructor(private _trackRecording: TrackRecording | null, private _isPaused: boolean) {
    }

    public get trackRecording(): TrackRecording | null {
        return this._trackRecording;
    }

    public get isPaused(): boolean {
        return this._isPaused;
    }
}