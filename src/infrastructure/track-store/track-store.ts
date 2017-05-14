import { Observable, Subject } from "rxjs/Rx";
import { Storage } from "@ionic/storage";

export abstract class TrackStore<TTrack> {
    private tracks: TTrack[] = [];

    private tracksChangedSubject = new Subject<TTrack[]>();

    protected constructor(private storage: Storage, private storageKey: string) {
    }

    public get tracksChanged(): Observable<TTrack[]> {
        return this.tracksChangedSubject;
    }

    public async storeTrack(track: TTrack): Promise<void> {
        await this.storage.ready();

        const trackRecordingIndex = this.tracks.indexOf(track);
        if (trackRecordingIndex > -1) {
            this.tracks[trackRecordingIndex] = track;
        } else {
            this.tracks.push(track);
        }

        await this.saveStore();
    }

    public async deleteStoredTrack(track: TTrack): Promise<void> {
        await this.storage.ready();

        const trackRecordingIndex = this.tracks.indexOf(track);
        if (trackRecordingIndex > -1) {
            this.tracks.splice(trackRecordingIndex, 1);

            await this.saveStore();
        }
    }

    protected abstract buildTrackInstance(TTrackLike: any): TTrack;

    public async getTracks(): Promise<TTrack[]> {
        await this.storage.ready();

        let archivedTrackRecordings = <TTrack[]>await this.storage.get(this.storageKey);
        archivedTrackRecordings = archivedTrackRecordings || [];

        this.tracks = archivedTrackRecordings.map(_ => this.buildTrackInstance(_));

        return this.tracks;
    }

    public async clearStore(): Promise<void> {
        await this.storage.ready();
        await this.storage.remove(this.storageKey);

        this.tracks = [];

        this.tracksChangedSubject.next(this.tracks);
    }

    private async saveStore(): Promise<void> {
        await this.storage.ready();
        await this.storage.set(this.storageKey, this.tracks);

        this.tracksChangedSubject.next(this.tracks);
    }
}