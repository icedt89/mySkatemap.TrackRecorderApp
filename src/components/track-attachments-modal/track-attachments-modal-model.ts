import { Exception } from "../../infrastructure/exception";
import { TrackAttachment } from "../../infrastructure/track-attachment";

export class TrackAttachmentsModalModel {
    private _attachmentsChanged = false;

    public constructor(public attachments: TrackAttachment[] = []) {
    }

    public get attachmentsChanged(): boolean {
        return this._attachmentsChanged;
    }

    public removeAttachment(attachment: TrackAttachment): void {
        const index = this.attachments.indexOf(attachment);
        if (index === -1) {
            throw new Exception("Attachment not found.");
        }

        this.attachments.splice(index, 1);

        this._attachmentsChanged = true;
    }

    public addAttachment(trackAttachment: TrackAttachment): void {
        this.attachments.push(trackAttachment);

        this._attachmentsChanged = true;
    }
}