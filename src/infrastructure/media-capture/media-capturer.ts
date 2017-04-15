import { Injectable } from "@angular/core";
import { Camera, CameraOptions } from "@ionic-native/camera";
import { CapturedMediaResult } from "./captured-media-result";

@Injectable()
export class MediaCapturer {
    public constructor(private camera: Camera) {
    }

    public captureCameraImage(): Promise<CapturedMediaResult> {
        return this.camera.getPicture(<CameraOptions>{
            destinationType: 0,
            correctOrientation: true
        }).then((result: string) => new CapturedMediaResult(result));
    }

    public selectLibraryImage(): Promise<CapturedMediaResult> {
        return this.camera.getPicture(<CameraOptions>{
            destinationType: 0,
            sourceType: 0,
            correctOrientation: true
        }).then((result: string) => new CapturedMediaResult(result));
    }
}