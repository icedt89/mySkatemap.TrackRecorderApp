import { ArchivedTrackRecording } from "../archived-track-recording";
import { Injectable } from "@angular/core";
import { TrackStore } from "./track-store";
import { Storage } from "@ionic/storage";

@Injectable()
export class ArchivedTrackRecordingStore extends TrackStore<ArchivedTrackRecording> {
    public constructor(storage: Storage) {
        super(storage, "TrackStore.ArchivedTrackRecordings");
    }

    protected buildTrackInstance(TTrackLike: any): ArchivedTrackRecording {
        return ArchivedTrackRecording.fromLike(TTrackLike);
    }
}