import { TrackAttachment } from "../../infrastructure/track-attachment";

export class TrackAttachmentsModalModel {
    public constructor(public attachments: TrackAttachment[] = []) {
    }

    public removeAttachment(attachment: TrackAttachment): void {
        const index = this.attachments.indexOf(attachment);
        if (index === -1) {
            return;
        }

        this.attachments.splice(index, 1);
    }
}