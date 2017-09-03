import { Inject } from "@angular/core";
import { ILogger } from "../logging/ilogger";
import { ArchivedTrackRecording } from "../archived-track-recording";
import { Injectable } from "@angular/core";
import { TrackStore } from "./track-store";
import { Storage } from "@ionic/storage";

@Injectable()
export class ArchivedTrackRecordingStore extends TrackStore<ArchivedTrackRecording> {
    public constructor(storage: Storage, @Inject("Logger") logger: ILogger) {
        super(storage, "TrackStore.ArchivedTrackRecordings");

        logger.warn("ArchivedTrackRecordingStore constructed");
    }

    protected buildTrackInstance(TTrackLike: any): ArchivedTrackRecording {
        return ArchivedTrackRecording.fromLike(TTrackLike);
    }
}