export class TrackAttachment {
    public static fromLike(trackAttachmentLike: TrackAttachment): TrackAttachment {
        return Object.assign(new TrackAttachment(), trackAttachmentLike);
    }

    public constructor (public imageDataUrl: string | null = null) {
    }

    public comment: string | null;
}