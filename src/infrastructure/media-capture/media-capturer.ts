import { Injectable } from "@angular/core";
import { Camera, CameraOptions } from "@ionic-native/camera";
import { CapturedMediaResult } from "./captured-media-result";

@Injectable()
export class MediaCapturer {
    public constructor(private camera: Camera) {
    }

    public async captureCameraImage(): Promise<CapturedMediaResult> {
        const result = <string>await this.camera.getPicture(<CameraOptions>{
            destinationType: this.camera.DestinationType.DATA_URL,
            allowEdit: false,
            quality: 100,
            correctOrientation: true
        });

        return new CapturedMediaResult(result);
    }

    public async selectLibraryImage(): Promise<CapturedMediaResult> {
        const result = <string>await this.camera.getPicture(<CameraOptions>{
            destinationType: this.camera.DestinationType.DATA_URL,
            allowEdit: false,
            sourceType: this.camera.PictureSourceType.PHOTOLIBRARY,
            correctOrientation: true
        });

        return new CapturedMediaResult(result);
    }
}