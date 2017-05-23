import { ILogger } from "../logging/ilogger";
import { Inject } from "@angular/core";
import { Injectable } from "@angular/core";
import { TrackRecording } from "../track-recording";
import { TrackStore } from "./track-store";
import { Storage } from "@ionic/storage";

@Injectable()
export class TrackRecordingStore extends TrackStore<TrackRecording> {
    public constructor(storage: Storage, @Inject("Logger") logger: ILogger) {
        super(storage, "TrackStore.TrackRecordings");

        logger.warn("TrackRecordingStore constructed");
    }

    protected buildTrackInstance(TTrackLike: any): TrackRecording {
        return TrackRecording.fromLike(TTrackLike);
    }
}