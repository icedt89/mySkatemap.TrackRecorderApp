import { Exception } from "../../infrastructure/exception";
import { TrackAttachment } from "../../infrastructure/track-attachment";

export class TrackAttachmentsModalModel {
    public constructor(public attachments: TrackAttachment[] = []) {
    }

    public removeAttachment(attachment: TrackAttachment): void {
        const index = this.attachments.indexOf(attachment);
        if (index === -1) {
            throw new Exception("Attachment not found.");
        }

        this.attachments.splice(index, 1);
    }
}