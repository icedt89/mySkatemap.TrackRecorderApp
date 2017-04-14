import { Injectable } from "@angular/core";
import { File } from "@ionic-native/file";
import { MediaCapture, MediaFile, CaptureImageOptions, CaptureVideoOptions, CaptureError } from "@ionic-native/media-capture";

@Injectable()
export class MediaCapturer {
    public constructor(private mediaCapture: MediaCapture,
        private file: File) {
    }

    public captureImage(limit = 1): Promise<string[]> {
        return this.mediaCapture.captureImage(<CaptureImageOptions>{
            limit: limit
        }).then((mediaFiles: MediaFile[]) => {
            const promises = mediaFiles.map(mediaFile => {
                const mediaFilePath = mediaFile.fullPath.replace(mediaFile.name, "");

                return this.file.readAsDataURL(mediaFilePath, mediaFile.name).then(dataUrl => this.file.removeFile(mediaFilePath, mediaFile.name).then(() => dataUrl));
            });

            return Promise.all(promises);
        }, (error: CaptureError) => {
            if (+error.code === 3) {
                return [];
            }
        });
    }
}