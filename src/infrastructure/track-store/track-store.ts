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

    public storeTrack(track: TTrack): Promise<void> {
        return this.storage.ready().then(() => {
            const trackRecordingIndex = this.tracks.indexOf(track);
            if (trackRecordingIndex > -1) {
                this.tracks[trackRecordingIndex] = track;
            } else {
                this.tracks.push(track);
            }

            return this.saveStore();
        });
    }

    public deleteStoredTrack(track: TTrack): Promise<void> {
        return this.storage.ready().then(() => {
            const trackRecordingIndex = this.tracks.indexOf(track);
            if (trackRecordingIndex > -1) {
                this.tracks.splice(trackRecordingIndex, 1);

                return this.saveStore();
            }
        });
    }

    protected abstract buildTrackInstance(TTrackLike: any): TTrack;

    public getTracks(): Promise<TTrack[]> {
        return <Promise<any>>this.storage.ready().then(() => this.storage.get(this.storageKey).then((archivedTrackRecordings: TTrack[]) => {
            archivedTrackRecordings = archivedTrackRecordings || [];

            this.tracks = archivedTrackRecordings.map(_ => this.buildTrackInstance(_));

            return this.tracks;
        }));
    }

    public clearStore(): Promise<void> {
        return <Promise<any>>this.storage.ready()
            .then(() => this.storage.remove(this.storageKey))
            .then(() => this.tracks = [])
            .then(() => this.tracksChangedSubject.next(this.tracks));
    }

    private saveStore(): Promise<void> {
        return this.storage.set(this.storageKey, this.tracks)
            .then(() => this.tracksChangedSubject.next(this.tracks));
    }
}