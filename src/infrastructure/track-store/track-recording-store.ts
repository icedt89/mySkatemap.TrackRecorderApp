import { Injectable } from "@angular/core";
import { TrackRecording } from "../track-recording";
import { TrackStore } from "./track-store";
import { Storage } from "@ionic/storage";

@Injectable()
export class TrackRecordingStore extends TrackStore<TrackRecording> {
    public constructor(storage: Storage) {
        super(storage, "TrackStore.TrackRecordings");
    }

    protected buildTrackInstance(TTrackLike: any): TrackRecording {
        return TrackRecording.fromLike(TTrackLike);
    }
}