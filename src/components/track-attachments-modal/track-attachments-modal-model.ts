import { Exception } from "../../infrastructure/exception";
import { TrackAttachment } from "../../infrastructure/track-attachment";

export class TrackAttachmentsModalModel {
    private _attachmentsChanged = false;

    public constructor(public attachments: TrackAttachment[] = [], private isReadonly = false) {
    }

    public get attachmentsChanged(): boolean {
        return this._attachmentsChanged;
    }

    public removeAttachment(attachment: TrackAttachment): void {
        if (this.isReadonly) {
            throw new Exception("Model is read only.");
        }

        const index = this.attachments.indexOf(attachment);
        if (index === -1) {
            throw new Exception("Attachment not found.");
        }

        this.attachments.splice(index, 1);

        this._attachmentsChanged = true;
    }

    public addAttachment(trackAttachment: TrackAttachment): void {
        if (this.isReadonly) {
            throw new Exception("Model is read only.");
        }

        this.attachments.push(trackAttachment);

        this._attachmentsChanged = true;
    }
}